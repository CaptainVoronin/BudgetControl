package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.budgetcontrol.zentypes.WidgetParamsConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class WidgetPinnedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if (extras.containsKey(MainActivity.BUNDLE_KEY_WIDGET)) {
            Log.i(this.getClass().getName(), "[onReceive] We are going to pin a widget");

            int appId = extras.getInt(MainActivity.BUNDLE_KEY_APP_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appId == AppWidgetManager.INVALID_APPWIDGET_ID)
                throw new InvalidParameterException("The APP WIDGET ID is incorrect");

            String buff = extras.getString(MainActivity.BUNDLE_KEY_WIDGET);

            try {
                JSONObject job = new JSONObject(buff);
                WidgetParams widget = WidgetParamsConverter.toWidget(job);
                widget.setAppId(appId);
                Log.i(this.getClass().getName(), "[onReceive] Widget app_id=" + appId + " is going to be pinned");
                updateWidget(context, widget);
                sendOkToMainActivity(context, widget );
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "[onReceive] " + e.getMessage());
                sendError( context, e );
            }
        }
        else
        {
            Log.e(this.getClass().getName(), "[onReceive] Intent doesn't have BUNDLE_KEY_WIDGET. Nothing to pin");
            throw new InvalidParameterException( "Intent doesn't have BUNDLE_KEY_WIDGET" );
        }
    }

    private void sendError(Context context, Exception e)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction( Intent.ACTION_SEND );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.putExtra( MainActivity.BUNDLE_KEY_PIN_ERROR, e.getMessage() );
        context.startActivity( intent );
    }

    private void sendOkToMainActivity(Context context, WidgetParams widget) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction( Intent.ACTION_SEND );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.putExtra( MainActivity.BUNDLE_KEY_EXIT_APP, true );
        intent.putExtra( MainActivity.BUNDLE_KEY_NEW_WIDGET_TITLE, widget.getTitle() );

        context.startActivity( intent );
    }

    private void updateWidget(@NotNull Context context, @NotNull WidgetParams widget) throws SQLiteException,
            MalformedURLException
    {
        BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
        bcdbHelper.insertWidgetParams(widget);

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
            ZenMoneyClient client = new ZenMoneyClient(new URL(settings.getParameterAsString("url")),
                    settings.getParameterAsString("token"),
                    handler);

            long timestamp = AWidgetViewMaker.calculateStartDate(widget.getStartPeriod());
            client.loadTransactions(timestamp);
    }
}
