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
        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_amount_limit_widget);
        views.setTextViewText(R.id.tvAmount, formatAmount(getWidget().getCurrentAmount()));
        views.setTextViewText(R.id.tvTitle, getWidget().getTitle());
        views.setTextViewText(R.id.tvPeriodName, formatAmount(getWidget().getLimitAmount()) );

        views.setInt(R.id.MainLayout, "setBackgroundColor", getWidget().getAmountParams().getBackColor().toArgb() );

        views.setInt(R.id.tvAmount, "setBackgroundColor", getWidget().getAmountParams().getBackColor().toArgb() );

        views.setInt(R.id.tvTitle, "setTextColor", getWidget().getTitleParams().getFontColor().toArgb() );
        views.setInt(R.id.tvTitle, "setBackgroundColor", getWidget().getTitleParams().getBackColor().toArgb());

        views.setInt(R.id.tvPeriodName, "setTextColor", getWidget().getPeriodParams().getFontColor().toArgb() );
        views.setInt(R.id.tvPeriodName, "setBackgroundColor", getWidget().getPeriodParams().getBackColor().toArgb());

        Color color = getScaleColor(getContext(), getWidget().getLimitAmount(), getWidget().getCurrentAmount());
        views.setInt(R.id.tvAmount, "setTextColor", color.toArgb());
        setOnClickReaction( views, R.id.tvAmount );
        return views;
    }

    public static final Color getScaleColor(Context context, double amountLimit, double currentAmount) {
        double percent = 100 * currentAmount / amountLimit;
        if (percent >= 100)
            return Color.valueOf(context.getColor(R.color.cbb0404));
        else if (percent >= 68 && percent <= 100)
            return Color.valueOf(context.getColor(R.color.c000000));
        else if (percent >= 54 && percent < 68)
            return Color.valueOf(context.getColor(R.color.c484848));
        else if (percent >= 40 && percent < 54)
            return Color.valueOf(context.getColor(R.color.c717171));
        else if (percent >= 26 && percent < 40)
            return Color.valueOf(context.getColor(R.color.ca1a1a1));
        else if (percent >= 13 && percent < 26)
            return Color.valueOf(context.getColor(R.color.cececec));
        else
            return Color.valueOf(context.getColor(R.color.cffffff));
    }
}
