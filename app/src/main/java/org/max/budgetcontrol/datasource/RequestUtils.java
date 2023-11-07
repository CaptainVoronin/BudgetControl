package org.max.budgetcontrol.datasource;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static String getInitialDiffRequestBody() throws JSONException {
        Map<String, Object> map = new HashMap<>();
        map.put("currentClientTimestamp", Long.valueOf(  System.currentTimeMillis() / 1000L ) );
        map.put("serverTimestamp", 0);
        JSONObject job = new JSONObject( map );
        JSONArray arr = new JSONArray();
        arr.put( ZenEntities.tag );
        job.put( "forceFetch", ( Object ) arr );
        return job.toString();
    }

    public static String getDiffRequestBody(long time)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("currentClientTimestamp", Long.valueOf(  System.currentTimeMillis() / 1000L ) );
        map.put("serverTimestamp", time);
        JSONObject job = new JSONObject( map );
        //job.put( ZenEntities.transaction.toString(), new JSONArray() );
        System.out.println( job.toString() );
        return job.toString();
    }

}
