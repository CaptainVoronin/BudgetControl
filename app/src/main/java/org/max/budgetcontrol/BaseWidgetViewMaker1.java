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

    private String getPeriodMessage()
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
