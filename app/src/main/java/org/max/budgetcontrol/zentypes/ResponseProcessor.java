package org.max.budgetcontrol.zentypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private static List<Category> makeCategoryTree(List<Category> categories) {
        Category parent;
        Map<UUID, Category> cTree = new HashMap<>();

        // Take top level categories
        cTree = categories.stream().filter( category -> category.getParent() == null ).collect(Collectors.toMap( Category::getId, category -> category ) );

        // Fill categories with child ones
        List<Category> children = categories.stream().filter( category -> category.getParent() != null ).toList();

        Iterator<UUID> it = cTree.keySet().iterator();
        while( it.hasNext() )
        {
            UUID id = it.next();
            cTree.get( id ).setChild( children.stream().filter( ch -> ch.getParent().equals( id ) ).toList() );
        }

        // Make the sorted list
        List<Category> set = cTree.values().stream().sorted( new Category.CategoryComparator() ).toList();

        // Sort child categories
        set.forEach( category -> { if( category.getChild() != null ) category.setChild( category.getChild().stream().sorted( new Category.CategoryComparator() ).toList() ); } );

        return set;
    }

}
