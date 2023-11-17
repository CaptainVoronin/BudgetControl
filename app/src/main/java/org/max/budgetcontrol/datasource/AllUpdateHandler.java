package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;
import org.max.budgetcontrol.zentypes.Transaction;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import okhttp3.Call;

public class AllUpdateHandler implements IZenClientResponseHandler {
    Context context;
    AppWidgetManager appWidgetManager;
    int[] appWidgetId;

    public AllUpdateHandler(Context context, AppWidgetManager appWidgetManager, int[] appWidgetId)
    {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId.clone();
    }

    @Override
    public void processResponse(JSONObject jObject) throws JSONException {
        RemoteViews views;

        List<Category> categories = ResponseProcessor.getCategory( jObject );
        try {
            List<Transaction> transactions = ResponseProcessor.getTransactions( jObject, System.currentTimeMillis(),categories );
            for( int id : appWidgetId )
                new WidgetUpdater( context, appWidgetManager, id ).updateWidget( transactions );
        } catch (ParseException e) {
        }
    }

    @Override
    public void processError(Exception e) {

    }

}
