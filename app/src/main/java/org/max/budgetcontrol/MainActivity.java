package org.max.budgetcontrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ASecondCallback;
import org.max.budgetcontrol.datasource.AZenClientResponseHandler;
import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.datasource.ResponseProcessor;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.budgetcontrol.zentypes.WidgetParamsConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String CONNECTION_PROBLEM = "connection_problem";
    public static final String BUNDLE_KEY_NEW_WIDGET_TITLE = "NEW_WIDGET_TITLE";
    private static final int WIDGET_WAS_PINNED = 876876;
    public static final String BUNDLE_KEY_WIDGET = "pinnedWidget";
    public static final String BUNDLE_KEY_APP_ID = "appWidgetId";
    public static final String BUNDLE_KEY_EXIT_APP = "bc_action_exit_app";
    public static final String BUNDLE_KEY_PIN_ERROR = "bc_pin_widget_error";

    List<Category> categories;

    public SettingsHolder getSettings() {
        return settings;
    }

    SettingsHolder settings;

    BCDBHelper db;

    public WidgetParams getCurrentWidget() {
        return currentWidget;
    }

    WidgetParams currentWidget;

    public WidgetCategoryHolder getCategoryHolder() {
        return categoryHolder;
    }

    WidgetCategoryHolder categoryHolder;

    StartPeriodEncoding currentPeriodCode;

    ActivityResultLauncher<Intent> launcher;
    private CategoryLoaderHandler categoryLoaderHandler;

    AlertDialog loadCategoriesDialog;

    AlertDialog loadTransactionsDialog;

    ValueChangeListener titleListener;

    ValueChangeListener amountListener;

    public WidgetParamsStateListener getParamsStateListener() {
        return paramsStateListener;
    }

    WidgetParamsStateListener paramsStateListener;

    EditText edTitle;

    EditText edAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // There are no request codes
                    settings = new SettingsHolder(getApplicationContext());
                    if (settings.init())
                        loadCategories();
                    else
                        showSettings(true);
                });

        // Создать или открыть БД
        db = BCDBHelper.getInstance(getApplicationContext());

        settings = new SettingsHolder(getApplicationContext());
        boolean completeConfig = settings.init();

        ViewPager viewPager = findViewById(R.id.pager);
        BCPagerAdapter viewPagerAdapter = new BCPagerAdapter (getSupportFragmentManager(), this );
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout =  findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (extras != null) {
            int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            setActivityResult(Activity.RESULT_CANCELED, appWidgetId);

            currentWidget = getWidgetParams(appWidgetId);
        } else {
            currentWidget = new WidgetParams();
            currentWidget.setAppId(AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        bringWidgetToUI();

        if (completeConfig)
            loadCategories();
        else
            showSettings(true);
    }

    private void bringWidgetToUI() {
        edTitle = findViewById(R.id.edTitle);
        edTitle.setText(currentWidget.getTitle());

        configSpinner(currentWidget);

        edAmount = findViewById(R.id.edAmount);
        edAmount.setText("" + currentWidget.getLimitAmount());

    }

    private void pinNewWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName myProvider = new ComponentName(this, BCWidget.class);

        if (appWidgetManager.isRequestPinAppWidgetSupported()) {
            Intent intent = new Intent(this, WidgetPinnedReceiver.class);
            String json = packWidgetIntoBundle();
            if (json == null) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setMessage(R.string.internal_application_error)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());
                dlg.show();
                return;
            }
            intent.putExtra(BUNDLE_KEY_WIDGET, json);
            PendingIntent successCallback = PendingIntent.getBroadcast(
                    /* context = */ this,
                    /* requestCode = */ WIDGET_WAS_PINNED,
                    /* intent = */ intent,
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            boolean ret = appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
            if (!ret)
                Log.d(this.getClass().getName(), "[pinNewWidget] Launcher doesn't support widget pinning");
        }
    }

    private String packWidgetIntoBundle() {
        try {
            JSONObject job = WidgetParamsConverter.toJSON(currentWidget);
            return job.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    void configSpinner(WidgetParams widget) {
        Spinner sp = findViewById(R.id.spStartPeriod);
        sp.setAdapter(new StartPeriodSpinAdapter(getApplicationContext(), widget));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object obj = view.getTag();
                currentPeriodCode = (StartPeriodEncoding) obj;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp.setSelection(widget.getStartPeriod().number());
    }

    private void loadCategories() {
        lockUIForWaiting();
        Log.d(this.getClass().getName(), "[loadCategories]");
        categoryLoaderHandler = new CategoryLoaderHandler();
        try {
            ZenMoneyClient client = new ZenMoneyClient(
                    new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    categoryLoaderHandler);
            client.getAllCategories();
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dlg_load_layout, null);
            dlg.setView(dialogView);

            dlg.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                dialogInterface.dismiss();
                categoryLoaderHandler.cancelRequest();
            }));

            loadCategoriesDialog = dlg.create();
            loadCategoriesDialog.show();

        } catch (MalformedURLException e) {
            categoryLoaderHandler.processError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.action_bar_menu, menu);
        initListeners(menu.findItem(R.id.idSave));

        return true;
    }

    private void initListeners(MenuItem item) {

        paramsStateListener = new WidgetParamsStateListener(item);

        titleListener = new ValueChangeListener(edTitle, paramsStateListener, value -> {
            if (value == null)
                return false;
            return value.toString().trim().length() > 0;
        }) {
            @Override
            protected void valueChanged(boolean checkResult) {
                paramsStateListener.setTitleComplete(checkResult);
            }
        };


        amountListener = new ValueChangeListener(edAmount, paramsStateListener, value -> {
            if (value == null)
                return false;
            if (value.toString().trim().length() == 0) return false;

            double amount = -1;
            try {
                amount = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }

            return amount >= 0;
        }) {
            @Override
            protected void valueChanged(boolean checkResult) {
                paramsStateListener.setAmountLimitComplete(checkResult);
            }
        };

        categoryHolder = new WidgetCategoryHolder(paramsStateListener, currentWidget.getCategories());
        String buff = edAmount.getText().toString();
        try {
            double d = Double.parseDouble(buff);
            paramsStateListener.setAmountLimitComplete(d >= 0);
        } catch (NumberFormatException e) {
            paramsStateListener.setAmountLimitComplete(false);
        }

        buff = edTitle.getText().toString();
        paramsStateListener.setTitleComplete(buff.trim().length() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.idCancel) {
            exitApp();
            return true;
        } else if (item.getItemId() == R.id.idSave) {
            saveChanges();
            return true;
        } else if (item.getItemId() == R.id.idSettings) {
            showSettings(true);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    public void showSettings(boolean withError) {
        Intent intent = new Intent(this, SettingsActivity.class);
        if (withError)
            intent.putExtra(CONNECTION_PROBLEM, true);

        launcher.launch(intent);
    }

    private void saveChanges() {

        Log.d(this.getClass().getName(), "[saveChanges]");
        applyEnteredValues();

        // Виджет добавляется через приложение, у него еще нет APP ID
        // Он будет добавляться через WidgetPinnedReceiver
        if (currentWidget.getAppId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            pinNewWidget();
        } else {

            // Виджет или добавляется через ланчер,
            // или конфигурится.
            // и его надо или вставить в БД, или проапдейтить
            // Это зависит от значение ID
            if (currentWidget.getId() == WidgetParams.INVALID_WIDGET_ID)
                db.insertWidgetParams(currentWidget);
            else
                db.updateWidgetParams(currentWidget);

            AppWidgetManager wManager = AppWidgetManager.getInstance(getApplicationContext());
            List<WidgetParams> wds = new ArrayList<>();
            wds.add(currentWidget);

            UpdateSelectedWidgetsHandler handler =
                    new UpdateSelectedWidgetsHandler(getApplicationContext(),
                            wManager,
                            new int[]{currentWidget.getAppId()},
                            wds);
            handler.setAfterCallback(new AfterUpdateWidgetCallback());

            try {
                ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                        settings.getParameterAsString("token"),
                        handler);
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setView(R.layout.dlg_load_transactions_layout);
                loadTransactionsDialog = dlg.create();
                loadTransactionsDialog.show();

                long timestamp = AWidgetViewMaker.calculateStartDate(currentWidget.getStartPeriod());
                client.loadTransactions(timestamp);
            } catch (MalformedURLException e) {
                handler.processError(e);
            }
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
        }
    }

    private void applyEnteredValues() {
        currentWidget.setCategories(categoryHolder.cats);
        TextView tv = findViewById(R.id.edTitle);
        currentWidget.setTitle(tv.getText().toString());
        tv = findViewById(R.id.edAmount);
        String buff = tv.getText().toString();
        double val = Double.parseDouble(buff);
        currentWidget.setLimitAmount(val);
        currentWidget.setStartPeriod(currentPeriodCode);
    }

    private void exitApp() {
        finishAndRemoveTask();
    }

    private void setActivityResult(int result, int widgetId) {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(result, intent);
    }

    private WidgetParams getWidgetParams(int appWidgetId) {
        WidgetParams wp = db.loadWidgetParamsByAppId(appWidgetId);
        if (wp == null) {
            wp = new WidgetParams();
            wp.setAppId(appWidgetId);
        }
        return wp;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        UUID id = (UUID) compoundButton.getTag();
        if (checked)
            categoryHolder.add(id);
        else
            categoryHolder.remove(id);
    }

    class CategoryLoaderHandler extends AZenClientResponseHandler {

        @Override
        public void onNon200Code(@NotNull Response response) {
            if (response.code() == 401 || response.code() == 500)
                runOnUiThread(() -> {
                    loadCategoriesDialog.dismiss();
                    showSettings(true);
                });
        }

        @Override
        public void onResponseReceived(JSONObject jObject) throws JSONException {
            List<Category> cats = ResponseProcessor.getCategory(jObject);
            final List<Category> cs = ResponseProcessor.makeCategoryTree(cats);
            runOnUiThread(() -> {
                loadCategoriesDialog.dismiss();
                bringCategoryListToFront(cs);
            });
        }

        @Override
        public void processError(Exception e) {
            runOnUiThread(() -> {
                loadCategoriesDialog.dismiss();
                if (e instanceof java.net.UnknownHostException) {

                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                    dlg.setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                        dialog.dismiss();
                        showSettings(true);
                    });
                    dlg.setTitle(R.string.netwotk_error);
                    dlg.setMessage(e.getMessage());
                    dlg.show();
                }
            });
        }
    }

    private void bringCategoryListToFront(List<Category> cats) {
        categories = cats;
        List<Category> flatList = new ArrayList<>();
        for (Category c : categories) {
            flatList.add(c);
            if (c.getChild().size() != 0)
                flatList.addAll(c.getChild());
        }
        ListView lv = (ListView) findViewById(R.id.lvCategories);

        flatList = flatList.stream().filter(c -> c.isOutcome()).collect(Collectors.toList());
        List<UUID> widgetCats = null;
        if (currentWidget != null)
            widgetCats = currentWidget.getCategories();

        lv.setAdapter(new CategoryListViewAdapter(getApplicationContext(), MainActivity.this, flatList, widgetCats));
        unlockUIOnResult();
    }

    private void lockUIForWaiting() {

    }

    private void unlockUIOnResult() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(BUNDLE_KEY_EXIT_APP)) {
                boolean exitApp = extras.getBoolean(BUNDLE_KEY_EXIT_APP, false);
                String title = extras.getString(BUNDLE_KEY_NEW_WIDGET_TITLE, "");
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle(R.string.complete_dlg_title)
                        .setMessage(String.format(getString(R.string.widget_pinned_message), title))
                        .setPositiveButton(android.R.string.ok, (dialog, i) -> {
                            dialog.dismiss();
                            if (exitApp) {
                                Log.i(this.getClass().getName(), "[onCreate] Finish signal received. Exit");
                                finish();
                            }
                        });
                dlg.show();
            } else if (extras.containsKey(BUNDLE_KEY_PIN_ERROR)) {
                String message = extras.getString(BUNDLE_KEY_PIN_ERROR);
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle(R.string.error_title)
                        .setMessage(message)
                        .setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.dismiss());
                dlg.show();
            }
        }
    }

    class AfterUpdateWidgetCallback extends ASecondCallback {
        @Override
        public void action() {
            loadTransactionsDialog.dismiss();
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
            finishAndRemoveTask();
        }
    }

}