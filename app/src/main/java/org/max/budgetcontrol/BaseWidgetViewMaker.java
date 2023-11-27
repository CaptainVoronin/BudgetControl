package org.max.budgetcontrol;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class BaseWidgetViewMaker extends AWidgetViewMaker
{
    SimpleDateFormat sdf;

    public BaseWidgetViewMaker(Context context, WidgetParams widget)
    {
        super( context, widget );
        sdf = new SimpleDateFormat( "dd.MM.yyyy" );
    }

    @Override
    public RemoteViews getViews()
    {
        long startDate = calculateStartDate(getWidget().getStartPeriod());
        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.tvAmount, Double.toString(getWidget().getCurrentAmount()));
        views.setTextViewText(R.id.tvTitle, getWidget().getTitle() );
        String buff = sdf.format( new Date( startDate ) );
        views.setTextViewText(R.id.tvStartDate, buff );
        return views;
    }
}
