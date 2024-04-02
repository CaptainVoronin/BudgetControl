package org.max.budgetcontrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    public static final String CONNECTION_PROBLEM = "connection_problem";
    public static final String BUNDLE_KEY_NEW_WIDGET_TITLE = "NEW_WIDGET_TITLE";
    private static final int WIDGET_WAS_PINNED = 876876;
    public static final String BUNDLE_KEY_WIDGET = "pinnedWidget";
    public static final String BUNDLE_KEY_APP_ID = "appWidgetId";

    public static final String BUNDLE_KEY_WIDGET_ACTION = "showExpences";
    public static final String BUNDLE_KEY_EXIT_APP = "bc_action_exit_app";
    public static final String BUNDLE_KEY_PIN_ERROR = "bc_pin_widget_error";

    public SettingsHolder getSettings()
    {
        return settings;
    }

    SettingsHolder settings;

    BCDBHelper db;

    public WidgetParams getCurrentWidget()
    {
        return currentWidget;
    }

    WidgetParams currentWidget;

    WidgetCategoryHolder categoryHolder;

    StartPeriodEncoding currentPeriodCode;

    ActivityResultLauncher<Intent> launcher;

    AlertDialog loadTransactionsDialog;

    WidgetParamsStateListener paramsStateListener;

    BCPagerAdapter viewPagerAdapter;

    private List<SettingsCompleteListener> settingsCompleteListeners;

    public MainActivity()
    {
        settingsCompleteListeners = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
                    if (!settings.init())
                        showSettings(true);
                    else
                        runOnUiThread(() -> notifySettingsComplete());

                });

        // Создать или открыть БД
        db = BCDBHelper.getInstance(getApplicationContext());

        settings = new SettingsHolder(getApplicationContext());
        boolean completeConfig = settings.init();

        ViewPager viewPager = findViewById(R.id.pager);
        viewPagerAdapter = new BCPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        Log.d(MainActivity.class.getName(), "[onCreate]");
        if (extras != null)
        {
            int appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            setActivityResult(Activity.RESULT_CANCELED, appWidgetId);

            currentWidget = getWidgetParams(appWidgetId);
        } else
        {
            currentWidget = new WidgetParams();
            currentWidget.setAppId(AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (!completeConfig)
            showSettings(true);
    }

    private void pinNewWidget()
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName myProvider = new ComponentName(this, BCWidget.class);

        if (appWidgetManager.isRequestPinAppWidgetSupported())
        {
            Intent intent = new Intent(this, WidgetPinnedReceiver.class);
            String json = packWidgetIntoBundle();
            if (json == null)
            {
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

    private String packWidgetIntoBundle()
    {
        try
        {
            JSONObject job = WidgetParamsConverter.toJSON(currentWidget);
            return job.toString();
        } catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.action_bar_menu, menu);
        initListeners(menu.findItem(R.id.idSave));
        MenuItem itemAbout = menu.findItem(R.id.idAbout);
        itemAbout.setOnMenuItemClickListener(menuItem -> {
            showAboutDialog();
            return false;
        });
        return true;
    }

    private void showAboutDialog()
    {
        try
        {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            String message = "Budget control " + versionName;

            builder.setTitle(R.string.dlg_about_title)
                    .setNegativeButton(android.R.string.cancel,
                            (dialogInterface, i) -> dialogInterface.cancel())
                    .setMessage(message).setIcon(R.mipmap.ic_launcher);
            builder.show();

        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return;
        }

    }

    private void initListeners(MenuItem item)
    {

        paramsStateListener = new WidgetParamsStateListener(item);

        for (int i = 0; i < viewPagerAdapter.getCount(); i++)
        {
            ABCFragment fragment = (ABCFragment) viewPagerAdapter.getItem(i);
            fragment.initListeners(paramsStateListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.idCancel)
        {
            exitApp();
            return true;
        } else if (item.getItemId() == R.id.idSave)
        {
            saveChanges();
            return true;
        } else if (item.getItemId() == R.id.idSettings)
        {
            showSettings(true);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    public void showSettings(boolean withError)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        if (withError)
            intent.putExtra(CONNECTION_PROBLEM, true);

        launcher.launch(intent);
    }

    private void saveChanges()
    {

        Log.d(this.getClass().getName(), "[saveChanges]");
        applyEnteredValues();

        // Виджет добавляется через приложение, у него еще нет APP ID
        // Он будет добавляться через WidgetPinnedReceiver
        if (currentWidget.getAppId() == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            pinNewWidget();
        } else
        {

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

            try
            {
                ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                        settings.getParameterAsString("token"),
                        handler);
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setView(R.layout.dlg_load_transactions_layout);
                loadTransactionsDialog = dlg.create();
                loadTransactionsDialog.show();

                long timestamp = AWidgetViewMaker.calculateStartDate(currentWidget.getStartPeriod());
                client.loadTransactions(timestamp);
            } catch (MalformedURLException e)
            {
                handler.processError(e);
            }
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
        }
    }

    private void applyEnteredValues()
    {
        for (int i = 0; i < viewPagerAdapter.getCount(); i++)
            ((ABCFragment) viewPagerAdapter.getItem(i)).applyEnteredValues();
    }

    private void exitApp()
    {
        finishAndRemoveTask();
    }

    private void setActivityResult(int result, int widgetId)
    {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(result, intent);
    }

    private WidgetParams getWidgetParams(int appWidgetId)
    {
        WidgetParams wp = db.loadWidgetParamsByAppId(appWidgetId);
        if (wp == null)
        {
            wp = new WidgetParams();
            wp.setAppId(appWidgetId);
        }
        return wp;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            if (extras.containsKey(BUNDLE_KEY_EXIT_APP))
            {
                boolean exitApp = extras.getBoolean(BUNDLE_KEY_EXIT_APP, false);
                String title = extras.getString(BUNDLE_KEY_NEW_WIDGET_TITLE, "");
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle(R.string.complete_dlg_title)
                        .setMessage(String.format(getString(R.string.widget_pinned_message), title))
                        .setPositiveButton(android.R.string.ok, (dialog, i) -> {
                            dialog.dismiss();
                            if (exitApp)
                            {
                                Log.i(this.getClass().getName(), "[onCreate] Finish signal received. Exit");
                                finish();
                            }
                        });
                dlg.show();
            } else if (extras.containsKey(BUNDLE_KEY_PIN_ERROR))
            {
                String message = extras.getString(BUNDLE_KEY_PIN_ERROR);
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle(R.string.error_title)
                        .setMessage(message)
                        .setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.dismiss());
                dlg.show();
            } else if (extras.containsKey(BUNDLE_KEY_APP_ID))
            {

            }
        }
    }

    class AfterUpdateWidgetCallback extends ASecondCallback
    {
        @Override
        public void action()
        {
            loadTransactionsDialog.dismiss();
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
            finishAndRemoveTask();
        }
    }

    final public void notifySettingsComplete()
    {
        settingsCompleteListeners.iterator().forEachRemaining(listener -> listener.onSettingsComplete());
    }

    public void setSelectedCategoriesList(List<UUID> selectedList, boolean checked)
    {
        categoryHolder.set(selectedList);
    }

    final public void addSettingsListener( SettingsCompleteListener listener )
    {
        settingsCompleteListeners.add( listener );
    }

    public interface SettingsCompleteListener
    {
        void onSettingsComplete();
    }
}