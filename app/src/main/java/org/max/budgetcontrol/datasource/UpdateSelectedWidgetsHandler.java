package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.ResponseProcessor;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateSelectedWidgetsHandler implements IZenClientResponseHandler {
    Context context;
    AppWidgetManager appWidgetManager;
    int[] widgetIdList;

    public void setAfterCallback(ASecondCallback afterCallback) {
        this.afterCallback = afterCallback;
    }

    ASecondCallback afterCallback;

    public UpdateSelectedWidgetsHandler(Context context, AppWidgetManager appWidgetManager, int[] widgetIdList)
    {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widgetIdList = widgetIdList.clone();
    }

    @Override
    public void processResponse(JSONObject jObject) throws JSONException {
        BCDBHelper bcdbHelper = new BCDBHelper(context);
        bcdbHelper.open();
        List<WidgetParams> widgets = bcdbHelper.getWidgets( widgetIdList );
        //List<Category> categories = ResponseProcessor.getCategory( jObject );
        List<Integer> lost = new ArrayList<>();
        try {
            List<Transaction> transactions = ResponseProcessor.getTransactions( jObject, System.currentTimeMillis() );

            for( WidgetParams widget : widgets )
            {
                if(Arrays.stream(widgetIdList).filter( id -> id == widget.getAppId() ).findFirst().isPresent() )
                    new WidgetUpdater(context, appWidgetManager, widget).updateWidget(transactions);
                else
                    lost.add( widget.getAppId() );
            }

            if( lost.size() != 0 )
                bcdbHelper.deleteLost( lost );
            if( afterCallback != null )
                afterCallback.action();
        } catch (ParseException e) {

        }
    }

    // TODO: не реализовано
    @Override
    public void processError(Exception e) {
        e.printStackTrace();
    }
}
