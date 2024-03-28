package org.max.budgetcontrol;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public abstract class AWidgetViewMaker
{

    private final Context context;

    private final WidgetParams widget;

    public Context getContext() {
        return context;
    }

    public WidgetParams getWidget() {
        return widget;
    }

    public AWidgetViewMaker(Context context, WidgetParams widget) {
            this.context = context;
            this.widget = widget;
    }

    public abstract RemoteViews getViews( );

    public static long calculateStartDate(StartPeriodEncoding startPeriodCode)
    {
        Calendar current = Calendar.getInstance();
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);

        switch (startPeriodCode)
        {
            case year:
                current.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case month:
                current.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case week:
                current.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
        }
        Log.d( "org.max.budgetcontrol.AWidgetViewMaker", "[calculateStartDate] Date is " + current.getTime().toString());
        return current.getTimeInMillis();
    }

    protected final String formatAmount( double value )
    {
        DecimalFormatSymbols d = new DecimalFormatSymbols();
        return String.format("%,.0f", value);
    }

    protected final String getPeriodMessage()
    {
        int messageId;

        switch (getWidget().getStartPeriod())
        {
            case week:
                messageId = R.string.message_for_week;
                break;
            case month:
                messageId = R.string.message_for_month;
                break;
            case year:
                messageId = R.string.message_for_year;
                break;
            default:
                messageId = R.string.message_for_month;
        }
        return getContext().getString( messageId );
    }
}
