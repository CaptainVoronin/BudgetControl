package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.res.AssetManager;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenMoneyClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class BCWidget extends AppWidgetProvider
{
    enum State
    {
        unknown,
        waiting,
        hasdata,
        error
    }

    ZenMoneyClient client;

    State state;

    String token = "0yteuv8iwTQcJpaBQXJ3XDZ3nnh1RV";
    String url = "https://api.zenmoney.ru/v8/diff";

    public BCWidget()
    {
        state = State.unknown;
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {

        CharSequence widgetText = getStateText( context );
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.b_c_widget);
        views.setTextViewText(R.id.txt_reminder, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private CharSequence getStateText( Context context )
    {
        int id;

        switch ( state )
        {
            case error:
                id = R.string.state_error_text;
                break;
            case waiting:
                id = R.string.state_wating_text;
                break;
            default:
                id = R.string.state_unk_text;
        }
        return context.getString( id );
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        /*try
        {
            loadSettings(context);
        } catch (JSONException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }*/
    }

    private void loadSettings(Context context) throws JSONException, IOException
    {
        client = ZenMoneyClient.getInstance( new URL(url), token );
        client.getInitialData( new MoneyRequestCallback(context) );
        state = State.waiting;
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }

    class MoneyRequestCallback implements Callback
    {
        Context context;

        public MoneyRequestCallback( Context context )
        {
            this.context = context;
        }

        @Override
        public void onFailure(Call call, IOException e)
        {
            e.printStackTrace();
            state = State.error;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            String buff = response.body().string();
            try
            {
                JSONObject jo = new JSONObject( buff );
                state = State.hasdata;

            } catch (JSONException e)
            {
                state = State.error;
                throw new RuntimeException(e);
            }
        }
    }
}