package org.max.budgetcontrol;

import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;
import org.max.budgetcontrol.zentypes.WidgetParams;

/*
Формирует вид виджета, в котором задан лимит
 */
public class AmountLimitViewMaker extends AWidgetViewMaker {

    public AmountLimitViewMaker(Context context, WidgetParams widget) {
        super(context, widget);
    }

    @Override
    public RemoteViews getViews() {
        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.tvAmount, formatAmount(getWidget().getCurrentAmount()));
        views.setTextViewText(R.id.tvTitle, getWidget().getTitle());
        views.setTextViewText(R.id.tvStartDate, getPeriodMessage());
        Color color = getScaleColor(getContext(), getWidget().getLimitAmount(), getWidget().getCurrentAmount());
        views.setInt(R.id.tvAmount, "setTextColor", color.toArgb());
        return views;
    }

    public static final Color getScaleColor(Context context, double amountLimit, double currentAmount) {
        double percent = 100 * currentAmount / amountLimit;
        if (percent >= 100)
            return Color.valueOf(context.getColor(R.color.cbb0404));
        else if (percent >= 84 && percent <= 100)
            return Color.valueOf(context.getColor(R.color.c000000));
        else if (percent >= 70 && percent <= 83)
            return Color.valueOf(context.getColor(R.color.c484848));
        else if (percent >= 69 && percent <= 56)
            return Color.valueOf(context.getColor(R.color.c717171));
        else if (percent >= 42 && percent <= 55)
            return Color.valueOf(context.getColor(R.color.ca1a1a1));
        else if (percent >= 28 && percent <= 41)
            return Color.valueOf(context.getColor(R.color.cececec));
        else
            return Color.valueOf(context.getColor(R.color.cffffff));
    }
}
