package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.IErrorHandler;
import org.max.budgetcontrol.datasource.IResponseHandler;
import org.max.budgetcontrol.datasource.InitialRequestResponseHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;

import java.io.IOException;
import java.net.URL;

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
        init,
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
        try {
            initApp(context);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initApp(Context context) throws JSONException, IOException {
        getCategoryListFromServer( context );
    }

    private void getCategoryListFromServer(Context context) throws IOException, JSONException
    {
        loadConnectionParameters(context);
        client = ZenMoneyClient.getInstance( new URL(url), token );
        client.getInitialData( new MoneyRequestCallback( context, new InitialRequestResponseHandler(context)) );
    }

    private void loadConnectionParameters(Context context) {
        token = context.getResources().getString( R.string.token);
        url = context.getResources().getString( R.string.url);
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }

    class MoneyRequestCallback implements Callback
    {
        Context context;
        IErrorHandler errorHandler;
        IResponseHandler responseHandler;

        public MoneyRequestCallback( Context context, IResponseHandler responseHandler )
        {
            this.context = context;
            this.errorHandler = new DefaultErrorHandler();
            this.responseHandler = responseHandler;
        }

        public MoneyRequestCallback( Context context, IResponseHandler responseHandler, IErrorHandler errorHandler )
        {
            this.context = context;
            this.errorHandler = errorHandler;
            this.responseHandler = responseHandler;
        }

        @Override
        public void onFailure(Call call, IOException e)
        {
            errorHandler.handleError( call, e );
            state = State.error;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            String buff = response.body().string();
            try
            {
                JSONObject jo = new JSONObject( buff );
                responseHandler.processResponse( jo );
                state = State.hasdata;

            } catch (JSONException e)
            {
                state = State.error;
                throw new RuntimeException(e);
            }
        }
    }

    class DefaultErrorHandler implements IErrorHandler
    {
        public void handleError(Call call, IOException e)
        {
            state = State.error;
            String str =Log.getStackTraceString( e );
            Log.e( this.getClass().getName(), str  );
        }
    }
}