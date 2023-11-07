package org.max.budgetcontrol.datasource;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 57ORlyBnixp1bGhpBu0CcNkrY0qRRZ
 */
class ZenMoneyClient
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

   public JSONObject getInitialData() throws IOException, JSONException {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getInitialDiffRequestBody() );
      return doRequest( body );
   }

   public JSONObject getTransactionsFromDate( Date date ) throws IOException, JSONException {
      RequestBody body = RequestBody.create( JSON,  RequestUtils.getDiffRequestBody(date.getTime() ) );
      return doRequest( body );
   }

   protected JSONObject doRequest( RequestBody body ) throws IOException, JSONException {
      Request.Builder requestBuilder = getRequestBuilder();
      //RequestBody body = RequestBody.create( JSON,  RequestUtils.getDiffRequestBody(date.getTime() ) );
      Request req = requestBuilder.post( body ).build();
      try (Response response = httpClient.newCall(req).execute()) {
         return new JSONObject( response.body().string() );
      }
   }

}
