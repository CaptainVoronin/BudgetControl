package org.max.budgetcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.datasource.IZenClientResponseHandler;
import org.max.budgetcontrol.datasource.RequestUtils;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    List<Category> categories;
    SettingsHolder settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new SettingsHolder(getApplicationContext());
        settings.init();
        Intent intent = getIntent();

        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            configureWidget(appWidgetId);
        } else {
            prepareForNewWidget();
        }
    }

    private void prepareForNewWidget() {
        CategoryLoaderHandler categoryLoaderHandler = new CategoryLoaderHandler();
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

    private void configureWidget(int appWidgetId) {
        WidgetParams wp = getWidgetParams(appWidgetId);
    }

    private WidgetParams getWidgetParams(int appWidgetId) {
        return null;
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
        List<Category> res = new ArrayList<>();
        for ( Category c : categories)
        {
            res.add( c );
            if( c.getChild().size() != 0 )
                res.addAll( c.getChild() );
        }
        ListView lv = (ListView) findViewById(R.id.lvCategories);

        res = res.stream().filter( c -> c.isOutcome() ).collect(Collectors.toList());
        lv.setAdapter(new CategoryListViewAdapter(getApplicationContext(), res, null));
    }
}