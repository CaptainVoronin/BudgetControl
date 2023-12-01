package org.max.budgetcontrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import kotlin.NotImplementedError;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    public static final String CONNECTION_PROBLEM = "connection_problem";

    SettingsHolder settings;

    BCDBHelper db;

    public WidgetParams getCurrentWidget() {
        return currentWidget;
    }

    WidgetParams currentWidget;

    ActivityResultLauncher<Intent> launcher;

    AlertDialog loadCategoriesDialog;
    AlertDialog loadTransactionsDialog;

    public SettingsHolder getSettings() {
        return settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // There are no request codes
                    settings = new SettingsHolder(getApplicationContext());
                    if (!settings.init())
                        showSettings(true);
                    /*else
                        loadCategories();*/

                });

        // Создать или открыть БД
        db = BCDBHelper.getInstance(getApplicationContext());

        settings = new SettingsHolder(getApplicationContext());
        boolean completeConfig = settings.init();

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

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

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        BCPagerAdapter viewPagerAdapter = new BCPagerAdapter (getSupportFragmentManager(), this );
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout =  findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        if (!completeConfig)
            showSettings(true);
        /*
        else
            loadCategories();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.action_bar_menu, menu);
        return true;
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

    void showSettings(boolean withError)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        if( withError)
            intent.putExtra(  CONNECTION_PROBLEM, true );

        launcher.launch(intent);
    }

    private void saveChanges()
    {
        Log.d(this.getClass().getName(), "[saveChanges]");
        applyEnteredValues();

        if (currentWidget.getId() == WidgetParams.INVALID_WIDGET_ID)
            db.insertWidgetParams(currentWidget);
        else
            db.updateWidgetParams(currentWidget);

        AppWidgetManager wManager = AppWidgetManager.getInstance(getApplicationContext());
        UpdateSelectedWidgetsHandler handler =
                new UpdateSelectedWidgetsHandler(getApplicationContext(),
                        wManager,
                        new int[]{currentWidget.getAppId()});
        handler.setAfterCallback(new AfterUpdateWidgetCallback());
        try
        {
            ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    handler);
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setView( R.layout.dlg_load_transactions_layout );
            loadTransactionsDialog = dlg.create();
            loadTransactionsDialog.show();

            client.loadTransactions(Calendar.getInstance().getTime());
        } catch (MalformedURLException e)
        {
            handler.processError(e);
        }

    /*    AppWidgetProviderInfo myWidgetProviderInfo = new AppWidgetProviderInfo();
        ComponentName myProvider = myWidgetProviderInfo.provider;

        if (mAppWidgetManager.isRequestPinAppWidgetSupported()) {
            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the widget to be pinned. Note that, if the pinning
            // operation fails, your app isn't notified.
            Intent pinnedWidgetCallbackIntent = new Intent( getApplicationContext(), this.getClass() );

            // Configure the intent so that your app's broadcast receiver gets
            // the callback successfully. This callback receives the ID of the
            // newly-pinned widget (EXTRA_APPWIDGET_ID).
            PendingIntent successCallback = PendingIntent.getBroadcast( getApplicationContext(), 0,
                    pinnedWidgetCallbackIntent, PendingIntent.FLAG_IMMUTABLE);

            mAppWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
        }*/

        setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
        //finishAndRemoveTask();

    }

    private void applyEnteredValues() {
        throw new NotImplementedError( "applyEnteredValues is not implemented" );
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
}