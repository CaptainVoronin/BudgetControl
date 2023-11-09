package org.max.budgetcontrol.zentypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResponseProcessor {

    static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat( "yyyy-MM-dd" );
    }

    public static List<Category> getCategory( JSONObject obj ) throws JSONException {
        JSONArray arr = obj.getJSONArray(ZenEntities.tag.name());
        List<Category> cats = new ArrayList<>();

        for( int i = 0; i < arr.length(); i++ )
        {
            JSONObject element = arr.getJSONObject( i );
            cats.add(  Category.fromJSONObject( element ) );
        }

        return cats;
    }

    public static List<Transaction> getTransactions( JSONObject obj, long startTime, List<Category> cats ) throws JSONException, ParseException {

        JSONArray arr = obj.getJSONArray(ZenEntities.tag.name());
        List<Transaction> transactions = new ArrayList<>();

        for( int i = 0; i < arr.length(); i++ )
        {
            JSONObject element = arr.getJSONObject( i );
            Date dt = sdf.parse( element.getString( "date" ) );
            if( dt.getTime() >= startTime )
                transactions.add( Transaction.fromJSONObject( element, cats ) );
        }

        return transactions;
    }

}
