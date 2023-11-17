package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.AllUpdateHandler;
import org.max.budgetcontrol.datasource.IErrorHandler;
import org.max.budgetcontrol.datasource.IZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        AllUpdateHandler handler = new AllUpdateHandler( context, appWidgetManager, appWidgetIds );
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
        // Enter relevant functionality for when the last widget is disabled
    }

}