package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

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
   static ZenMoneyClient instance;
   static String token = "57ORlyBnixp1bGhpBu0CcNkrY0qRRZ";

   OkHttpClient httpClient;

   public static final MediaType JSON = MediaType.get("application/json");

   URL url;

   protected ZenMoneyClient(URL url, String token )
   {
      this.url = url;
      this.token = token;
      httpClient = new OkHttpClient();
   }

   public static ZenMoneyClient getInstance(URL url, String token)
   {
      if (instance == null)
         instance = new ZenMoneyClient(url, token );
      return instance;
   }

   Request.Builder getRequestBuilder()
   {
      return new Request.Builder()
              .url( url )
              .header("Content-Type", "application/json")
              .header( "Authorization", "Bearer " + token );
   }

   public void getInitialData(Callback callback) throws JSONException, IOException
   {
      RequestBody body = null;
      body = RequestBody.create( JSON,  RequestUtils.getInitialDiffRequestBody() );
      doRequest( body, callback );
   }

   public void getTransactionsFromDate( Callback callback, Date date ) throws IOException, JSONException {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getDiffRequestBody(date.getTime() ) );
      doRequest( body, callback);
   }

   protected void doRequest(RequestBody body, Callback callback) throws IOException, JSONException {
      Request.Builder requestBuilder = getRequestBuilder();
      //RequestBody body = RequestBody.create( JSON,  RequestUtils.getDiffRequestBody(date.getTime() ) );
      Request req = requestBuilder.post( body ).build();
      httpClient.newCall(req).enqueue(callback);
   }

}
