package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.ViewMakerFactory;
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

    public UpdateSelectedWidgetsHandler(Context context, AppWidgetManager appWidgetManager,
                                        int[] widgetIdList )
    {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widgetIdList = widgetIdList.clone();
    }

    @Override
    public void updateWidgets(JSONObject jObject) throws JSONException {
        List<Transaction> transactions = null;
        ViewMakerFactory factory = new ViewMakerFactory( context );

        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        List<WidgetParams> widgets = bcdbHelper.getWidgets( widgetIdList );
        List<Integer> lost = new ArrayList<>();
        try {

            if( jObject != null )
                transactions = ResponseProcessor.getTransactions( jObject );

            for( WidgetParams widget : widgets )
            {
                if(Arrays.stream(widgetIdList).filter( id -> id ==
                        widget.getAppId() ).findFirst().isPresent() )
                {
                    WidgetOnlineUpdater updater = new WidgetOnlineUpdater( context,
                            appWidgetManager,
                            factory.getViewMaker( widget ),
                            widget );
                    updater.updateWidget(transactions);
                }
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
