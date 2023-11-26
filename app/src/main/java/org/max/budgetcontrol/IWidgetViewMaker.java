package org.max.budgetcontrol;

import android.util.Log;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;

import java.util.Calendar;

public interface IWidgetViewMaker
{
    RemoteViews getViews( );

    default long calculateStartDate(StartPeriodEncoding startPeriodCode)
    {
        Calendar current = Calendar.getInstance();
        current.set(Calendar.HOUR, 0);
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
        Log.d(this.getClass().getName(), "[calculateStartDate] Date is " + current.getTime().toString());
        return current.getTimeInMillis();
    }
}
