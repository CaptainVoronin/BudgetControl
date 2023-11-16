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

public class AllUpdateHandler implements IResponseHandler, IErrorHandler {
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
            {
                views = getRemoteViews( jObject );
                appWidgetManager.updateAppWidget( id, views );
            }
        } catch (ParseException e) {
            views = getErrorView( e );
            appWidgetManager.updateAppWidget( appWidgetId, views );
        }
    }

    private RemoteViews getErrorView(ParseException e) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.txt_reminder, e.getLocalizedMessage() );
        return views;
    }

    private RemoteViews getRemoteViews(JSONObject jObject) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.txt_reminder, "updated");
        return views;
    }

    List<Transaction> filterTransactions(List<Category> cats, List<Transaction> transactions )
    {
        return transactions;
    }

    @Override
    public void handleError(Call call, IOException e) {

    }
}
