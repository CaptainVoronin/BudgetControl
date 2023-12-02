package org.max.budgetcontrol;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.WidgetOnlineUpdater;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WidgetPinnedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        Bundle extras = intent.getExtras();
        Log.i(this.getClass().getName(), "[onReceive] Widget was successfully pinned");

        if (extras.containsKey("widget_id"))
        {
            int id = extras.getInt("widget_id");
            int appId = extras.getInt("appWidgetId");

            Log.i(this.getClass().getName(), "[onReceive] Widget id=" + id + " is going to be updated");
            updateWidget(context, id, appId );
        }
    }

    private void updateWidget(Context context, int id, int appId)
    {
        AppWidgetManager wm = AppWidgetManager.getInstance(context);

        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        ViewMakerFactory factory = new ViewMakerFactory(context);
        WidgetParams widget = bcdbHelper.loadWidgetParamsById(id);
        widget.setAppId( appId );

        bcdbHelper.updateWidgetParams( widget );
        AppWidgetManager wManager = AppWidgetManager.getInstance(context);
        List<WidgetParams> wds = new ArrayList<>();

        wds.add(widget);

        UpdateSelectedWidgetsHandler handler =
                new UpdateSelectedWidgetsHandler(context,
                        wManager,
                        new int[]{widget.getAppId()},
                        wds);

        SettingsHolder settings = new SettingsHolder(context);
        settings.init();

        try
        {
            ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    handler);

            long timestamp = AWidgetViewMaker.calculateStartDate(widget.getStartPeriod());
            client.loadTransactions(timestamp);
        } catch (MalformedURLException e)
        {
            handler.processError(e);
        }
    }
}
