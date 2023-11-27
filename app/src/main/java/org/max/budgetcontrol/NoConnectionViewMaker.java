package org.max.budgetcontrol;

import android.content.Context;
import android.widget.RemoteViews;
import org.max.budgetcontrol.zentypes.WidgetParams;

public class NoConnectionViewMaker extends AWidgetViewMaker {
    public NoConnectionViewMaker(Context context, WidgetParams widget) {
        super(context,widget);
    }

    @Override
    public RemoteViews getViews() {
        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.tvAmount, "");
        views.setTextViewText(R.id.tvTitle, "----");
        views.setTextViewText(R.id.tvStartDate, "");
        return views;
    }
}
