package org.max.budgetcontrol;

import android.content.Context;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.WidgetParams;

class BaseWidgetViewMaker1 extends BaseWidgetViewMaker
{
    public BaseWidgetViewMaker1(Context context, WidgetParams widget)
    {
        super(context, widget);
    }

    @Override
    public RemoteViews getViews()
    {
        RemoteViews views = super.getViews();
        views.setTextViewText(R.id.tvStartDate, getPeriodMessage());
        return views;
    }
}
