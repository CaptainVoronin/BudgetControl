package org.max.budgetcontrol.datasource;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.Transaction;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class WidgetUpdater {
    Context context;
    AppWidgetManager appWidgetManager;
    WidgetParams widget;

    static SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy" );

    public WidgetUpdater(Context context, AppWidgetManager appWidgetManager, WidgetParams widget ) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.widget = widget;
    }

    public void updateWidget(List<Transaction> transactions)
    {
        long startDate = calculateStartDate( widget.getStartPeriod() );
        double amount = getAmount( transactions, startDate);
        RemoteViews view = getViews( amount, startDate );
        appWidgetManager.updateAppWidget( widget.getAppId(), view );
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
        Log.d( this.getClass().getName(), "[calculateStartDate] Date is " + current.getTime().toString());
        return current.getTimeInMillis();
    }

    private RemoteViews getViews(Double amount, long startDate) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.tvAmount, amount.toString() );
        views.setTextViewText(R.id.tvTitle, widget.getTitle() );
        String buff = sdf.format( new Date( startDate ) );
        views.setTextViewText(R.id.tvStartDate, buff );
        return views;
    }

    /**
     *
     * @param transactions
     * @return
     */
    private double getAmount(List<Transaction> transactions, long startDate) {
        double amount = transactions.stream().filter( t-> t.getDate().getTime() >= startDate )
                .filter( t-> t.hasCategory( widget.getCategories() ) )
                .mapToDouble( t -> t.getAmount() ).sum();
        return amount;
    }

    WidgetParams getWidgetParamsFromDB(  )
    {
        WidgetParams wp = new WidgetParams();

        return new WidgetParams();
    }
}