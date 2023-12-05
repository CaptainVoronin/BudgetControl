package org.max.budgetcontrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
    private static final int WINDGET_WAS_PINNED = 876876;
    public static final String BUNDLE_KEY_WIDGET = "pinnedWidget";
    public static final String BUNDLE_KEY_APP_ID = "appWidgetId";

    List<Category> categories;
    SettingsHolder settings;

    BCDBHelper db;

    WidgetParams currentWidget;

    WidgetCategoryHolder categoryHolder;

    StartPeriodEncoding carrentPeriodCode;

    ActivityResultLauncher<Intent> launcher;
    private CategoryLoaderHandler categoryLoaderHandler;

    AlertDialog loadCategoriesDialog;
    AlertDialog loadTransactionsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        if (extras != null) {
            int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            setActivityResult(Activity.RESULT_CANCELED, appWidgetId);
            makeWidgetParams(appWidgetId);
        } else {
            currentWidget = new WidgetParams();
            currentWidget.setAppId(AppWidgetManager.INVALID_APPWIDGET_ID);
            categoryHolder = new WidgetCategoryHolder(currentWidget.getCategories());
        }

        bringWidgetToUI();

        if (completeConfig)
            loadCategories();
        else
            showSettings(true);
    }

    private void bringWidgetToUI() {
        TextView tv = findViewById(R.id.edTitle);
        tv.setText(currentWidget.getTitle());
        tv = findViewById(R.id.edAmount);
        tv.setText("" + currentWidget.getLimitAmount());
        configSpinner(currentWidget);
    }

    private void pinNewWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName myProvider = new ComponentName(this, BCWidget.class);

        if (appWidgetManager.isRequestPinAppWidgetSupported()) {
            // Create the PendingIntent object only if your app needs to be notified
            // when the user chooses to pin the widget. Note that if the pinning
            // operation fails, your app isn't notified. This callback receives the ID
            // of the newly pinned widget (EXTRA_APPWIDGET_ID).
            Intent intent = new Intent(this, WidgetPinnedReceiver.class);
            String json = packWidgetIntoBundle();
            if (json == null) {

            }
            intent.putExtra(BUNDLE_KEY_WIDGET, json);
            PendingIntent successCallback = PendingIntent.getBroadcast(
                    /* context = */ this,
                    /* requestCode = */ WINDGET_WAS_PINNED,
                    /* intent = */ intent,
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
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
                carrentPeriodCode = (StartPeriodEncoding) obj;
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
        return true;
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

    private void showSettings(boolean withError) {
        Intent intent = new Intent(this, SettingsActivity.class);
        if (withError)
            intent.putExtra(CONNECTION_PROBLEM, true);

        launcher.launch(intent);
    }

    private void saveChanges() {
        Log.d(this.getClass().getName(), "[saveChanges]");
        applyEnteredValues();

        // Если это новый виджет, у него еще нет APP ID
        // Он будет добавляться через WidgetPinnedReceiver
        if (currentWidget.getAppId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            pinNewWidget();
            return;
        }

        // А это если виджет уже существовал,
        // и его надо просто проапдейтить
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

    private void applyEnteredValues() {
        currentWidget.setCategories(categoryHolder.cats);
        TextView tv = findViewById(R.id.edTitle);
        currentWidget.setTitle(tv.getText().toString());
        tv = findViewById(R.id.edAmount);
        String buff = tv.getText().toString();
        double val = Double.parseDouble(buff);
        currentWidget.setLimitAmount(val);
        currentWidget.setStartPeriod(carrentPeriodCode);
    }

    private void exitApp() {
        finishAndRemoveTask();
    }

    private void setActivityResult(int result, int widgetId) {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(result, intent);
    }

    private void makeWidgetParams(int appWidgetId) {
        currentWidget = getWidgetParams(appWidgetId);
        categoryHolder = new WidgetCategoryHolder(currentWidget.getCategories());
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

    class AfterUpdateWidgetCallback extends ASecondCallback {
        @Override
        public void action() {
            loadTransactionsDialog.dismiss();
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
            finishAndRemoveTask();
        }
    }

}