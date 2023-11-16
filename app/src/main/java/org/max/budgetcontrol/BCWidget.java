package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.AllUpdateHandler;
import org.max.budgetcontrol.datasource.IErrorHandler;
import org.max.budgetcontrol.datasource.IResponseHandler;
import org.max.budgetcontrol.datasource.InitialRequestResponseHandler;
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
        ZenMoneyClient client = new ZenMoneyClient( url, token );
        AllUpdateHandler handler = new AllUpdateHandler( context, appWidgetManager, appWidgetIds );
        try {
            client.getTransactionsFromDate( new MoneyRequestCallback( handler, handler ), new Date() );
        } catch (JSONException e) {
            updateWithError( context, appWidgetManager, appWidgetIds, e );
        }
    }

    private void updateWithError(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, JSONException e) {

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
        loadConnectionParameters( context );
        getCategoryListFromServer( context );
    }

    private void getCategoryListFromServer(Context context) throws IOException, JSONException
    {
        loadConnectionParameters(context);
        ZenMoneyClient client = new ZenMoneyClient( url, token );
        client.getInitialData( new MoneyRequestCallback( new InitialRequestResponseHandler(context)) );
    }

    private void loadConnectionParameters(Context context) throws MalformedURLException {
        SharedPreferences pr = context.getSharedPreferences( "app.properties", Context.MODE_PRIVATE );
        String strURL = pr.getString( context.getString( R.string.url ), null );
        token = pr.getString( context.getString( R.string.token ), null );

        if( strURL == null ) {
            token = context.getString(R.string.token_value);
            strURL = context.getString(R.string.url_value);
            SharedPreferences.Editor ed = pr.edit();
            ed.putString( context.getString( R.string.url ), strURL );
            ed.putString( context.getString( R.string.token ), token );
            ed.commit();
        }

        url = new URL( strURL );
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }

    class MoneyRequestCallback implements Callback
    {
        IErrorHandler errorHandler;
        IResponseHandler responseHandler;

        public MoneyRequestCallback( IResponseHandler responseHandler )
        {
           // this.context = context;
            this.errorHandler = new DefaultErrorHandler();
            this.responseHandler = responseHandler;
        }

        public MoneyRequestCallback( IResponseHandler responseHandler, IErrorHandler errorHandler )
        {
            //this.context = context;
            this.errorHandler = errorHandler;
            this.responseHandler = responseHandler;
        }

        @Override
        public void onFailure(Call call, IOException e)
        {
            errorHandler.handleError( call, e );
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            String buff = response.body().string();
            try
            {
                JSONObject jo = new JSONObject( buff );
                responseHandler.processResponse( jo );

            } catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    class DefaultErrorHandler implements IErrorHandler
    {
        public void handleError(Call call, IOException e)
        {
            String str = Log.getStackTraceString( e );
            Log.e( this.getClass().getName(), str  );
        }
    }
}