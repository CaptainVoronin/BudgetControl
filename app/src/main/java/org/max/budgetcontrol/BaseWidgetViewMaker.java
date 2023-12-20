package org.max.budgetcontrol;

import android.content.Context;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.WidgetParams;

class BaseWidgetViewMaker extends AWidgetViewMaker
{

    public BaseWidgetViewMaker(Context context, WidgetParams widget)
    {
        super( context, widget );
    }

    @Override
    public RemoteViews getViews()
    {
        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_widget);

        views.setTextViewText(R.id.tvAmount, formatAmount( getWidget().getCurrentAmount()));
        views.setInt(R.id.tvAmount, "setTextColor", getWidget().getAmountParams().getFontColor().toArgb() );
        int color = getWidget().getAmountParams().getBackColor().toArgb();
        views.setInt(R.id.tvAmount, "setBackgroundColor", color );

        views.setTextViewText(R.id.tvTitle, getWidget().getTitle() );
        views.setInt(R.id.tvTitle, "setTextColor", getWidget().getTitleParams().getFontColor().toArgb() );
        views.setInt(R.id.tvTitle, "setBackgroundColor", getWidget().getTitleParams().getBackColor().toArgb());

        views.setTextViewText(R.id.tvPeriodName, getPeriodMessage());
        views.setInt(R.id.tvPeriodName, "setTextColor", getWidget().getPeriodParams().getFontColor().toArgb() );
        views.setInt(R.id.tvPeriodName, "setBackgroundColor", getWidget().getPeriodParams().getBackColor().toArgb());

        return views;
    }
}
