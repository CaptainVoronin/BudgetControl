package org.max.budgetcontrol;

import android.content.Context;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.WidgetParams;

import java.text.SimpleDateFormat;
import java.util.Date;

class BaseWidgetViewMaker implements IWidgetViewMaker
{
    private final Context context;

    private final WidgetParams widget;

    SimpleDateFormat sdf;

    public BaseWidgetViewMaker(Context context, WidgetParams widget)
    {
        this.context = context;
        this.widget = widget;
        sdf = new SimpleDateFormat( "dd.MM.yyyy" );
    }

    @Override
    public RemoteViews getViews()
    {
        long startDate = calculateStartDate(widget.getStartPeriod());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.tvAmount, Double.toString(widget.getCurrentAmount()));
        views.setTextViewText(R.id.tvTitle, widget.getTitle() );
        String buff = sdf.format( new Date( startDate ) );
        views.setTextViewText(R.id.tvStartDate, buff );
        return views;
    }
}
