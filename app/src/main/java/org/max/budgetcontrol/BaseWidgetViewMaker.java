package org.max.budgetcontrol;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.max.budgetcontrol.zentypes.WidgetParams;

import static org.max.budgetcontrol.MainActivity.BUNDLE_KEY_APP_ID;
import static org.max.budgetcontrol.MainActivity.BUNDLE_KEY_WIDGET_ACTION;

class BaseWidgetViewMaker extends AWidgetViewMaker
{

    public BaseWidgetViewMaker(Context context, WidgetParams widget)
    {
        super( context, widget );
    }

    @Override
    public RemoteViews getViews()
    {
        Bundle extras = new Bundle();
        Intent clickIntent = new Intent( getContext(), org.max.budgetcontrol.MainActivity.class );

        extras.putInt( BUNDLE_KEY_APP_ID, getWidget().getAppId() );
        extras.putBoolean( BUNDLE_KEY_WIDGET_ACTION, true );
        clickIntent.putExtras( extras );

        PendingIntent clickPI = PendingIntent.getActivity(getContext(), 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE, extras);

        RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.b_c_widget);
        views.setInt(R.id.MainLayout, "setBackgroundColor", getWidget().getAmountParams().getBackColor().toArgb() );

        views.setTextViewText(R.id.tvAmount, formatAmount( getWidget().getCurrentAmount()));
        views.setInt(R.id.tvAmount, "setTextColor", getWidget().getAmountParams().getFontColor().toArgb() );
        views.setOnClickPendingIntent( R.id.tvAmount, clickPI);

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
