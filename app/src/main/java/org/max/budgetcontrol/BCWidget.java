package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import org.json.JSONException;
import org.max.budgetcontrol.datasource.UpdateSelectedWidgetsHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;
import org.max.budgetcontrol.db.BCDBHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class BCWidget extends AppWidgetProvider
{
    String token;
    URL url;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        try {
            initApp(context);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UpdateSelectedWidgetsHandler handler = new UpdateSelectedWidgetsHandler( context, appWidgetManager, appWidgetIds );
        ZenMoneyClient client = new ZenMoneyClient( url, token, handler );
        client.updateWidgets( new Date() );
    }

    private void updateWithError(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, JSONException e) {

    }

    @Override
    public void onEnabled(Context context)
    {
    }

    private void initApp(Context context) throws JSONException, IOException {
        SettingsHolder settingsHolder = new SettingsHolder( context );
        settingsHolder.init();
        token = settingsHolder.getParameterAsString( "token" );
        String strURL = settingsHolder.getParameterAsString( "url" );
        url = new URL( strURL );
    }

    @Override
    public void onDisabled(Context context)
    {
        BCDBHelper bcdbHelper = BCDBHelper.getInstance(  context );
        bcdbHelper.clearWidgets();
    }

}