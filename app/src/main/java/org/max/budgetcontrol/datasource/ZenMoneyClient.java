package org.max.budgetcontrol.datasource;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 57ORlyBnixp1bGhpBu0CcNkrY0qRRZ
 */
public class ZenMoneyClient {
    String token;
    OkHttpClient httpClient;

    public static final MediaType JSON = MediaType.get("application/json");

    URL url;

    AZenClientResponseHandler handler;

    public ZenMoneyClient(URL url, String token, AZenClientResponseHandler handler) {
        this.url = url;
        this.token = token;
        this.handler = handler;
        httpClient = new OkHttpClient();
    }

    Request.Builder getRequestBuilder() {
        return new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token);
    }

   /*public void getInitialData(Callback callback) throws JSONException
   {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getInitialRequestBody() );
      doRequest( body, callback );
   }*/

    /**
     * Получает транзакции, совершенные после заданной параметром даты
     *
     * @param date дата, от которой надо брать тразакции
     * @throws JSONException
     */
    public void loadTransactions(Date date) {
        Log.i(this.getClass().getName(), "[loadTransactions] ");
        try {
            RequestBody body = RequestBody.create(JSON, RequestUtils.getDiffRequestBody(date.getTime()));
            doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
    }

    public void getAllCategories() {
        try {
            Log.i(this.getClass().getName(), "[getAllCategories] ");
            RequestBody body = RequestBody.create(JSON, RequestUtils.getCategoriesRequestBody());
            doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
    }

    protected UUID doRequest(RequestBody body, Callback callback) {
        UUID tag = UUID.randomUUID();
        Request.Builder requestBuilder = getRequestBuilder();
        Request req = requestBuilder.tag( tag ).post(body).build();
        Log.i(this.getClass().getName(), "[doRequest] " + body.toString());
        httpClient.newCall(req).enqueue(callback);
        Log.i( this.getClass().getName(), "[cancel] Request id " + tag.toString() + "has been enqueued");
        if( handler != null )
            handler.setRequestTag( this, tag );
        return tag;
    }

    public void cancel( UUID tag )
    {
        for (Call call : httpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag))
            {
                call.cancel();
                Log.i( this.getClass().getName(), "[cancel] Request id " + tag.toString() + " has removed from the queue");
            }
        }

        //B) go through the running calls and cancel if the tag matches:
        for (Call call : httpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag))
            {
                call.cancel();
                Log.i( this.getClass().getName(), "[cancel] Request id " + tag.toString() + " has been aborted");
            }
        }
    }

    public void checkConnection() {
        try {
            Log.i(this.getClass().getName(), "[checkConnection] ");
            RequestBody body = RequestBody.create(JSON, RequestUtils.getEmptyBody());
            doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
    }
}

class InternalCallback implements Callback {
    AZenClientResponseHandler zenResponseHandler;

    public InternalCallback(AZenClientResponseHandler zenResponseHandler) {
        this.zenResponseHandler = zenResponseHandler;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.i(this.getClass().getName(), "[onFailure]");
        zenResponseHandler.processError(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Log.i(this.getClass().getName(), "[onResponse] HTTP " + response.code());
        if (response.code() == 200) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(response.body().string());
                zenResponseHandler.onResponseReceived(jsonObject);
            } catch (JSONException e) {
                Log.i(this.getClass().getName(), "[onResponse] Exception " + e.getClass().getName());
                zenResponseHandler.processError(e);
            }
        } else
            zenResponseHandler.onNon200Code(response);
    }

}
