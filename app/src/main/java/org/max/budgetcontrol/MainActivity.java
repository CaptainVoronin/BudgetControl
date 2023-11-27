package org.max.budgetcontrol;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ASecondCallback;
import org.max.budgetcontrol.datasource.IZenClientResponseHandler;
import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{
    public static final String CONNECTION_PROBLEM = "connection_problem";
    List<Category> categories;
    SettingsHolder settings;

    BCDBHelper db;

    WidgetParams currentWidget;

    WidgetCategoryHolder categoryHolder;

    StartPeriodEncoding carrentPeriodCode;

    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        // There are no request codes
                        settings = new SettingsHolder(getApplicationContext());
                        if (settings.init())
                            loadCategories();
                        else
                            showSetting(true);
                    }
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
            makeWidgetParams(appWidgetId);
        } else
        {
            currentWidget = new WidgetParams();
            currentWidget.setAppId(AppWidgetManager.INVALID_APPWIDGET_ID);
            categoryHolder = new WidgetCategoryHolder(currentWidget.getCategories());
        }

        configSpinner(currentWidget);

        if (completeConfig)
            loadCategories();
        else
            showSetting(true);
    }

    void configSpinner(WidgetParams widget)
    {
        Spinner sp = findViewById(R.id.spStartPeriod);
        sp.setAdapter(new StartPeriodSpinAdapter(getApplicationContext(), widget));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Object obj = view.getTag();
                carrentPeriodCode = (StartPeriodEncoding) obj;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });
        sp.setSelection(widget.getStartPeriod().number());
    }

    private void loadCategories()
    {
        lockUIForWaiting();
        Log.d(this.getClass().getName(), "[loadCategories]");
        CategoryLoaderHandler categoryLoaderHandler = new CategoryLoaderHandler();
        try
        {
            ZenMoneyClient client = new ZenMoneyClient(
                    new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    categoryLoaderHandler);
            client.getAllCategories();
        } catch (MalformedURLException e)
        {
            categoryLoaderHandler.processError(e);
        }
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
            showSetting(true);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void showSetting(boolean withError)
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

    private void applyEnteredValues()
    {
        currentWidget.setCategories(categoryHolder.cats);
        TextView tv = (TextView) findViewById(R.id.edTitle);
        currentWidget.setTitle(tv.getText().toString());
        tv = (TextView) findViewById(R.id.edAmount);
        String buff = tv.getText().toString();
        double val = Double.parseDouble(buff);
        currentWidget.setLimitAmount(val);
        currentWidget.setStartPeriod(carrentPeriodCode);
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

    private void makeWidgetParams(int appWidgetId)
    {
        currentWidget = getWidgetParams(appWidgetId);
        categoryHolder = new WidgetCategoryHolder(currentWidget.getCategories());
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
    {
        UUID id = (UUID) compoundButton.getTag();
        if (checked)
            categoryHolder.add(id);
        else
            categoryHolder.remove(id);
    }

    class CategoryLoaderHandler implements IZenClientResponseHandler
    {

        @Override
        public void onNon200Code(Response responze) {
            if( responze.code() == 401 )
                runOnUiThread(() -> showSetting(true) );
        }

        @Override
        public void updateWidgets(JSONObject jObject) throws JSONException
        {
            List<Category> cats = ResponseProcessor.getCategory(jObject);
            final List<Category> cs = ResponseProcessor.makeCategoryTree(cats);
            runOnUiThread(()-> bringCategoryListToFront(cs));
        }

        @Override
        public void processError(Exception e)
        {

        }
    }

    private void bringCategoryListToFront(List<Category> cats)
    {
        categories = cats;
        List<Category> flatList = new ArrayList<>();
        for (Category c : categories)
        {
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

    private void lockUIForWaiting()
    {

    }

    private void unlockUIOnResult()
    {

    }

    class AfterUpdateWidgetCallback extends ASecondCallback
    {

        @Override
        public void action()
        {
            setActivityResult(Activity.RESULT_OK, currentWidget.getAppId());
            finishAndRemoveTask();
        }
    }
}