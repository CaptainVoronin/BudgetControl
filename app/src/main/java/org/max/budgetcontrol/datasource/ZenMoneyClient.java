package org.max.budgetcontrol.datasource;

import org.json.JSONException;

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
public class ZenMoneyClient
{
   String token;

   OkHttpClient httpClient;

   public static final MediaType JSON = MediaType.get("application/json");

   URL url;

   public ZenMoneyClient(URL url, String token )
   {
      this.url = url;
      this.token = token;
      httpClient = new OkHttpClient();
   }

   Request.Builder getRequestBuilder()
   {
      return new Request.Builder()
              .url( url )
              .header("Content-Type", "application/json")
              .header( "Authorization", "Bearer " + token );
   }

   public void getInitialData(Callback callback) throws JSONException
   {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getInitialRequestBody() );
      doRequest( body, callback );
   }

   public void updateWidgets(Callback callback, Date date ) throws JSONException {
      RequestBody body = null;
      body = RequestBody.create( JSON,  RequestUtils.getDiffRequestBody(date.getTime() ) );
      doRequest(body, callback);
   }

   public void getAllCategories(Callback callback ) throws JSONException {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getCategoriesRequestBody( ) );
      doRequest(body, callback);
   }

   protected void doRequest(RequestBody body, Callback callback){
      Request.Builder requestBuilder = getRequestBuilder();
      Request req = requestBuilder.post( body ).build();
      httpClient.newCall(req).enqueue(callback);
   }

   class InternalCallback implements Callback{

      @Override
      public void onFailure(Call call, IOException e) {

      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {

      }
   }
}
