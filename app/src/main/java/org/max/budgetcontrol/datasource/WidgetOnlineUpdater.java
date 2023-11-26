package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.IWidgetViewMaker;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.List;

public class WidgetOnlineUpdater
{
    private final IWidgetViewMaker viewMaker;
    private Context context;
    private AppWidgetManager appWidgetManager;
    private WidgetParams widget;

    public final Context getContext()
    {
        return context;
    }

    public final AppWidgetManager getAppWidgetManager()
    {
        return appWidgetManager;
    }

    public final WidgetParams getWidget()
    {
        return widget;
    }

    public WidgetOnlineUpdater(Context context, AppWidgetManager appWidgetManager, IWidgetViewMaker viewMaker, WidgetParams widget ) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widget = widget;
        this.viewMaker = viewMaker;
    }

    public void updateWidget(List<Transaction> transactions)
    {
        long startDate = viewMaker.calculateStartDate( getWidget().getStartPeriod() );
        calculateAmount( transactions, startDate);
        RemoteViews view = viewMaker.getViews( );
        getAppWidgetManager().updateAppWidget( getWidget().getAppId(), view );
        if( transactions != null )
            saveCash(  );
    }

    private void saveCash()
    {
        BCDBHelper db = BCDBHelper.getInstance( getContext());
        db.updateWidgetParams( getWidget() );
    }

    protected void calculateAmount(List<Transaction> transactions, long startDate) {
        if( transactions != null )
        {
            double amount = transactions.stream().filter(t -> t.getDate().getTime() >= startDate)
                    .filter(t -> t.hasCategory(getWidget().getCategories()))
                    .mapToDouble(t -> t.getAmount()).sum();
            getWidget().setCurrentAmount( amount );
        }
    }
}