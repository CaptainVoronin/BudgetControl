package org.max.budgetcontrol.datasource;

import org.json.JSONObject;

import java.io.IOException;

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
class ZenMoneyClient
{
   static ZenMoneyClient instance;
   static String token = "57ORlyBnixp1bGhpBu0CcNkrY0qRRZ";

   OkHttpClient httpClient;

   String url;

   protected ZenMoneyClient(String url)
   {
      this.url = url;
      httpClient = new OkHttpClient();
   }

   public static ZenMoneyClient getInstance(String url)
   {
      if (instance == null)
         instance = new ZenMoneyClient(url);
      return instance;
   }

   public void getData( Callback callback ){
      Request request = new Request.Builder()
              .url( url )
              .header("Content-Type", "application/json")
              .header( "Authorization", "Bearer " + token )
              .build();
      httpClient.newCall(request).enqueue( callback );
   }

}
