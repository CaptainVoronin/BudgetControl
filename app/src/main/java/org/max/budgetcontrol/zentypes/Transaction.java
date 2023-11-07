package org.max.budgetcontrol.zentypes;

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
    List<Category> category;

    static SimpleDateFormat sdf;

    static{
        sdf = new SimpleDateFormat( "yyyy-MM-dd" );
    }

    protected Transaction( UUID id, double amount, Date date, List<Category> category)
    {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public static Transaction newTransaction(JSONObject obj, List<Category> categories ) throws ParseException, JSONException {

        UUID uuid = UUID.fromString( obj.getString( "id" ) );
        if( obj.isNull( "tag") )
            return null;

        JSONArray oTags = obj.getJSONArray( "tag" );
        List<UUID> uuids = new ArrayList<>();

        for( int i = 0; i < oTags.length(); i++ )
            uuids.add( UUID.fromString( oTags.getString( i ) ) );

        List<Category> tagCats = new ArrayList<>();
        Transaction.getCategories( uuids, categories, tagCats );

        double inc = obj.getDouble( "income" );
        double out = obj.getDouble( "outcome" );
        double amount = out != 0 ? (out * -1) : inc;
        String buff = obj.getString( "date" );
        Date date = sdf.parse( buff );
        return new Transaction( uuid, amount, date, tagCats );
    }

    private static void getCategories(List<UUID> trTagIds, List<Category> categories, List<Category> transCats ) {
        for ( Category c : categories ) {
            if( trTagIds.contains( c.getId() ) )
                transCats.add( c );
            if( c.getChild() != null )
                getCategories( trTagIds, c.getChild(), transCats );
        }
    }

    public UUID getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public List<Category> getCategory() {
        return category;
    }

    public boolean hasCategory( UUID categoryId )
    {
        return category.stream().filter( c-> c.getId().equals( categoryId ) ).count() != 0l;
    }

}
