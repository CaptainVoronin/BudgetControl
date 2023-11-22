package org.max.budgetcontrol.zentypes;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Transaction {
    UUID id;
    double amount;
    Date date;
    List<UUID> category;

    static SimpleDateFormat sdf;

    static{
        sdf = new SimpleDateFormat( "yyyy-MM-dd" );
    }

    protected Transaction( UUID id, double amount, @NotNull Date date, @NotNull List<UUID> category)
    {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public static Transaction fromJSONObject(@NotNull JSONObject obj) throws ParseException, JSONException {

        UUID uuid = UUID.fromString( obj.getString( "id" ) );
        if( obj.isNull( "tag") ) {
            Log.w( "org.max.budgetcontrol.zentypes.Transaction", "Transaction hasn't tags");
            Log.d( "org.max.budgetcontrol.zentypes.Transaction", obj.toString());
            return null;
        }
        JSONArray oTags = obj.getJSONArray( "tag" );
        List<UUID> uuids = new ArrayList<>();

        for( int i = 0; i < oTags.length(); i++ )
            uuids.add( UUID.fromString( oTags.getString( i ) ) );

        double inc = obj.getDouble( "income" );
        double out = obj.getDouble( "outcome" );
        double amount = out != 0 ? (out * -1) : inc;
        String buff = obj.getString( "date" );
        Date date = sdf.parse( buff );
        return new Transaction( uuid, amount, date, uuids );
    }

    /*private static void getCategories(List<UUID> trTagIds, List<Category> categories, List<Category> transCats ) {
        for ( Category c : categories ) {
            if( trTagIds.contains( c.getId() ) )
                transCats.add( c );
            if( c.getChild() != null )
                getCategories( trTagIds, c.getChild(), transCats );
        }
    }*/

    public UUID getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public List<UUID> getCategory() {
        return category;
    }
/*

    public boolean hasCategory( UUID categoryId )
    {
        return category.contains( categoryId );
    }
*/

    public boolean hasCategory( List<UUID> categoryIds )
    {
        for( UUID uuid : categoryIds )
        {
            if( category.contains( uuid ) )
                return true;
        }
        return false;
    }
}
