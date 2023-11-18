package org.max.budgetcontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.IZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    List<Category> categories;
    SettingsHolder settings;

    BCDBHelper db;

    WidgetParams currentWidget;

    WidgetCategoryHolder categoryHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new SettingsHolder(getApplicationContext());
        settings.init();

        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        // Создать или открыть БД
        db();

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        configureWidget(appWidgetId);
        loadCategories();

    }

    private void db()
    {
        db = new BCDBHelper( getApplicationContext() );
        db.open();
    }

    private void loadCategories() {
        CategoryLoaderHandler categoryLoaderHandler = new CategoryLoaderHandler(  );
        try {
            ZenMoneyClient client = new ZenMoneyClient(
                    new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    categoryLoaderHandler);
            client.getAllCategories();
        } catch (MalformedURLException e) {
            categoryLoaderHandler.processError( e );
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.idCancel1 )
        {
            exitApp();
            return true;
        } else if ( item.getItemId() ==  R.id.idSave1 )
        {
            saveChanges();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    private void saveChanges()
    {
        currentWidget.setCategories( categoryHolder.cats );

        if( currentWidget.id == WidgetParams.INVALID_WIDGET_ID )
            db.insertWidgetParams( currentWidget );
        else
            db.updateWidgetParams( currentWidget );

        AppWidgetManager mAppWidgetManager =
                getApplicationContext().getSystemService(AppWidgetManager.class);

        AppWidgetProviderInfo myWidgetProviderInfo = new AppWidgetProviderInfo();
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
        }
    }

    private void exitApp()
    {
        finishAndRemoveTask();
        System.exit(0);
    }

    private void configureWidget(int appWidgetId) {
        currentWidget = getWidgetParams(appWidgetId);
        categoryHolder = new WidgetCategoryHolder( currentWidget.getCategories() );
    }

    private WidgetParams getWidgetParams(int appWidgetId) {
        WidgetParams wp = db.loadWidgetParamsByAppId( appWidgetId );
        if( wp == null )
        {
            wp = new WidgetParams();
            wp.setAppId( appWidgetId );
        }
        return wp;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
    {
        UUID id = ( UUID ) compoundButton.getTag();
        if( checked )
            categoryHolder.add( id );
        else
            categoryHolder.remove( id );
    }

    class CategoryLoaderHandler implements IZenClientResponseHandler {

        @Override
        public void processResponse(JSONObject jObject) throws JSONException {
            List<Category> cats = ResponseProcessor.getCategory(jObject);
            final List<Category> cs = ResponseProcessor.makeCategoryTree(cats);
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    bringCategoryListToFront(cs);
                }
            });
        }

        @Override
        public void processError(Exception e) {

        }
    }

    private void bringCategoryListToFront(List<Category> cats) {
        categories = cats;
        List<Category> flatList = new ArrayList<>();
        for ( Category c : categories)
        {
            flatList.add( c );
            if( c.getChild().size() != 0 )
                flatList.addAll( c.getChild() );
        }
        ListView lv = (ListView) findViewById(R.id.lvCategories);

        flatList = flatList.stream().filter( c -> c.isOutcome() ).collect(Collectors.toList());
        List<UUID> widgetCats = null;
        if( currentWidget != null )
            widgetCats = currentWidget.getCategories();

        lv.setAdapter(new CategoryListViewAdapter(getApplicationContext(), MainActivity.this, flatList, widgetCats ));
    }
}