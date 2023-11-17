package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;
import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;
import java.util.Calendar;
import java.util.List;

public class WidgetUpdater {
    Context context;
    AppWidgetManager appWidgetManager;
    int widgetId;

    public WidgetUpdater(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widgetId = widgetId;
    }

    public void updateWidget(List<Transaction> transactions)
    {
        WidgetParams wp = getWidgetParamsFromDB();
        long startDate = calculateStartDate( wp.getStartPeriodCode() );
        double amount = getAmount( transactions, startDate);
        RemoteViews view = getViews( amount );
        appWidgetManager.updateAppWidget( widgetId, view );
    }

    private long calculateStartDate(StartPeriodEncoding startPeriodCode) {
        Calendar current = Calendar.getInstance();
        current.set( Calendar.HOUR, 0);
        current.set( Calendar.MINUTE, 0);
        current.set( Calendar.SECOND, 0 );

        switch ( startPeriodCode )
        {
            case year:
                current.set( Calendar.DAY_OF_YEAR, 1 );
                break;
            case month:
                current.set( Calendar.DAY_OF_MONTH, 1 );
                break;
            case week:
                current.set( Calendar.DAY_OF_WEEK, Calendar.MONDAY );
                break;
        }
        return current.getTimeInMillis();
    }

    private RemoteViews getViews(Double amount) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.txt_reminder, amount.toString() );
        return views;
    }

    /**
     *
     * @param transactions
     * @return
     */
    private double getAmount(List<Transaction> transactions, long startDate) {
        double amount = transactions.stream().filter( t-> t.getDate().getTime() >= startDate ).mapToDouble( t -> t.getAmount() ).sum();
        return amount;
    }

    WidgetParams getWidgetParamsFromDB(  )
    {
        WidgetParams wp = new WidgetParams();

        return new WidgetParams();
    }
}