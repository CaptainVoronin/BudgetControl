package org.max.budgetcontrol.datasource;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.zentypes.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static byte[] getInitialRequestBody() throws JSONException
    {
        return getSpecificRequestBody( null, 0l );
    }

    public static byte[] getDiffRequestBody(long time) throws JSONException
    {
        return getSpecificRequestBody( null, time );
    }

    public static byte[] getAccountsRequestBody() throws JSONException
    {
        return getSpecificRequestBody( ZenEntities.account, System.currentTimeMillis() / 1000l );
    }

    public static byte[] getNewTransactionRequestBody(Transaction transaction) throws JSONException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("currentClientTimestamp", System.currentTimeMillis() / 1000L );
        map.put("serverTimestamp", 0l );
        JSONObject job = new JSONObject( map );
        JSONArray trAr = new JSONArray();
        trAr.put( transaction.toJSONObject() );
        job.put( "transaction", trAr );
        return job.toString().getBytes(StandardCharsets.UTF_8);
    }


    public static byte[] getCategoriesRequestBody() throws JSONException
    {
        return getSpecificRequestBody( ZenEntities.tag, System.currentTimeMillis() );
    }

    static byte[] getSpecificRequestBody( ZenEntities entity, long serverTimestampValue ) throws JSONException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("currentClientTimestamp", System.currentTimeMillis() / 1000L );
        map.put("serverTimestamp", serverTimestampValue );
        JSONObject job = new JSONObject( map );
        if( entity != null )
        {
            JSONArray arr = new JSONArray();
            arr.put( entity.toString() );
            job.put( "forceFetch", ( Object ) arr );
        }
        return job.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getEmptyBody() throws JSONException {
        return getSpecificRequestBody( null, System.currentTimeMillis() );
    }
}
