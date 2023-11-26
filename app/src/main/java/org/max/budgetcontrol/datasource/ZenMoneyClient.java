package org.max.budgetcontrol.datasource;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

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

    IZenClientResponseHandler handler;

    public ZenMoneyClient(URL url, String token, IZenClientResponseHandler handler) {
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
    public void updateWidgets(Date date) {
        Log.i( this.getClass().getName(), "[updateWidgets] ");
        try {
            RequestBody body = RequestBody.create(JSON, RequestUtils.getDiffRequestBody(date.getTime()));
            doRequest(body, new InternalCallback(handler));
        } catch (JSONException e) {
            handler.processError(e);
        }
    }

    public void getAllCategories() {
        try {
            Log.i( this.getClass().getName(), "[getAllCategories] ");
            RequestBody body = RequestBody.create(JSON, RequestUtils.getCategoriesRequestBody());
            doRequest(body, new InternalCallback(handler));
        } catch (JSONException e) {
            handler.processError(e);
        }
    }

    protected void doRequest(RequestBody body, Callback callback) {
        Request.Builder requestBuilder = getRequestBuilder();
        Request req = requestBuilder.post(body).build();
        Log.i( this.getClass().getName(), "[doRequest] " + body.toString());
        httpClient.newCall(req).enqueue(callback);
    }
}

class InternalCallback implements Callback {
    IZenClientResponseHandler zenResponseHandler;

    public InternalCallback(IZenClientResponseHandler zenResponseHandler) {
        this.zenResponseHandler = zenResponseHandler;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.i( this.getClass().getName(), "[onFailure]");
        zenResponseHandler.processError(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Log.i( this.getClass().getName(), "[onResponse] ");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response.body().string());
            zenResponseHandler.updateWidgets(jsonObject);
        } catch (JSONException e) {
            Log.i( this.getClass().getName(), "[onResponse] Exception " + e.getClass().getName());
            zenResponseHandler.processError(e);
        }
    }
}
