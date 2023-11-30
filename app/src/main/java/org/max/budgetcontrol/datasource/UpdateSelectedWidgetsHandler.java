package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.ViewMakerFactory;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Response;

public class UpdateSelectedWidgetsHandler extends AZenClientResponseHandler
{
    Context context;
    AppWidgetManager appWidgetManager;
    int[] widgetIdList;

    public void setAfterCallback(ASecondCallback afterCallback)
    {
        this.afterCallback = afterCallback;
    }

    ASecondCallback afterCallback;

    public UpdateSelectedWidgetsHandler(Context context, AppWidgetManager appWidgetManager,
                                        int[] widgetIdList)
    {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widgetIdList = widgetIdList.clone();
    }

    @Override
    public void onNon200Code(Response response)
    {
        ViewMakerFactory factory = new ViewMakerFactory(context);
        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        List<WidgetParams> widgets = bcdbHelper.getWidgets(widgetIdList);

        if (response.code() == 401)
        {
            for (WidgetParams widget : widgets)
            {
                if (Arrays.stream(widgetIdList).filter(id -> id ==
                        widget.getAppId()).findFirst().isPresent())
                {
                    factory.getViewMaker(401, widget);
                    new WidgetOnlineUpdater(context,
                            appWidgetManager,
                            factory.getViewMaker(401, widget),
                            widget).updateWidget(null);
                }
            }
        }

    }

    @Override
    public void onResponseReceived(JSONObject jObject) throws JSONException
    {
        List<Transaction> transactions = null;
        ViewMakerFactory factory = new ViewMakerFactory(context);

        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        try
        {
            List<WidgetParams> widgets = bcdbHelper.getWidgets(widgetIdList);

            Log.i( this.getClass().getName(), "[onResponseReceived] " + widgets.size() + " widget(s) is going to be updated" );

            List<Integer> lost = new ArrayList<>();
            if (jObject != null)
                transactions = ResponseProcessor.getTransactions(jObject);

            Log.i( this.getClass().getName(), "[onResponseReceived] Loaded " + transactions.size() + " transactions" );

            for (WidgetParams widget : widgets)
            {
                if (Arrays.stream(widgetIdList).filter(id -> id ==
                        widget.getAppId()).findFirst().isPresent())
                {
                    WidgetOnlineUpdater updater = new WidgetOnlineUpdater(context,
                            appWidgetManager,
                            factory.getViewMaker(200, widget),
                            widget);
                    updater.updateWidget(transactions);
                } else
                    lost.add(widget.getAppId());
            }

            if (lost.size() != 0)
                bcdbHelper.deleteLost(lost);
            if (afterCallback != null)
                afterCallback.action();
        } catch (Exception e)
        {
            Log.e( this.getClass().getName(), "[onResponseReceived] Exception: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    // TODO: не реализовано
    @Override
    public void processError(Exception e)
    {

        if (isNetWorkError(e))
        {
            Log.i( this.getClass().getName(), "[processError] There is network error. " + e.getMessage() + " Load data from cash" );
            loadFromCash();
        }
        else
        {
            e.printStackTrace();
        }
    }

    private void loadFromCash()
    {
        Log.i( this.getClass().getName(), "[loadFromCash] " );
        ViewMakerFactory factory = new ViewMakerFactory(context);
        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        List<WidgetParams> widgets = bcdbHelper.getWidgets(widgetIdList);
        for (WidgetParams widget : widgets)
        {
            WidgetOnlineUpdater updater = new WidgetOnlineUpdater(context,
                    appWidgetManager,
                    factory.getViewMaker(200, widget),
                    widget);
            updater.updateWidget(null);
        }
    }
}
