package org.max.budgetcontrol.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
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
     */
    public @Nullable UUID loadTransactions( @NonNull long timestamp) {
        UUID tag = null;
        Log.i(this.getClass().getName(), "[loadTransactions] ");
        try {
            RequestBody body = RequestBody.create(JSON, RequestUtils.getDiffRequestBody(timestamp));
            tag = doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
        return tag;
    }

    public @Nullable UUID getAllCategories() {
        UUID tag = null;
        try {
            Log.i(this.getClass().getName(), "[getAllCategories] ");
            RequestBody body = RequestBody.create(JSON, RequestUtils.getCategoriesRequestBody());
            tag = doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
        return tag;
    }

    protected @Nullable UUID doRequest(RequestBody body, Callback callback) {
        UUID tag = UUID.randomUUID();
        Request.Builder requestBuilder = getRequestBuilder();
        Request req = requestBuilder.tag( tag ).post(body).build();
        Log.i(this.getClass().getName(), "[doRequest] " + req.url().toString() );
        httpClient.newCall(req).enqueue(callback);
        Log.i( this.getClass().getName(), "[doRequest] Request id " + tag + " has been enqueued");
        if( handler != null )
            handler.setRequestTag( this, tag );
        return tag;
    }

    public void cancel( @NonNull UUID tag )
    {
        for (Call call : httpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag))
            {
                call.cancel();
                Log.i( this.getClass().getName(), "[cancel] Request id " + tag + " has removed from the queue");
            }
        }

        //B) go through the running calls and cancel if the tag matches:
        for (Call call : httpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag))
            {
                call.cancel();
                Log.i( this.getClass().getName(), "[cancel] Request id " + tag + " has been aborted");
            }
        }
    }

    public @Nullable UUID checkConnection() {
        UUID tag = null;
        try {
            Log.i(this.getClass().getName(), "[checkConnection] ");
            RequestBody body = RequestBody.create(JSON, RequestUtils.getEmptyBody());
            tag = doRequest(body, new InternalCallback(handler));
        } catch (Exception e) {
            handler.processError(e);
        }
        return tag;
    }
}

class InternalCallback implements Callback {
    AZenClientResponseHandler zenResponseHandler;

    public InternalCallback(@NonNull  AZenClientResponseHandler zenResponseHandler) {
        this.zenResponseHandler = zenResponseHandler;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.i(this.getClass().getName(), "[onFailure] " + e.getMessage());
        zenResponseHandler.processError(e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Log.i(this.getClass().getName(), "[onResponse] HTTP " + response.code());
        if (response.code() == 200) {
            JSONObject jsonObject;
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