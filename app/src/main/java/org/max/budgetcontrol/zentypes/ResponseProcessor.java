package org.max.budgetcontrol.zentypes;


import android.util.Log;

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
        if( obj.has( ZenEntities.tag.name() ) )
        {
            JSONArray arr = obj.getJSONArray(ZenEntities.tag.name());
            List<Category> cats = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject element = arr.getJSONObject(i);
                cats.add(Category.fromJSONObject(element));
            }
            return cats;
        }
        else
            return null;
    }

    public static List<Transaction> getTransactions( JSONObject obj ) throws JSONException, ParseException {
        final String deleted = "deleted";

        List<Transaction> transactions = null;
        if( obj.has( ZenEntities.transaction.name() ))
        {
            JSONArray arr = obj.getJSONArray(ZenEntities.transaction.name());
            transactions = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject element = arr.getJSONObject(i);

                if( element.has( deleted))
                    if( element.getBoolean( deleted) == true )
                    {
                        Log.i( "org.max.budgetcontrol.ResponseProcessor", "[getTransactions] Transaction deleted. Skip");
                        continue;
                    }
                Transaction tr = Transaction.fromJSONObject(element);
                if( tr != null )
                    transactions.add( tr );
                else
                    Log.w( "org.max.budgetcontrol.zentypes.ResponseProcessor", "Can't make transaction without category" );
            }
        }
        return transactions;
    }

    public static List<Category> makeCategoryTree(List<Category> categories) {
        Map<UUID, Category> cTree = new HashMap<>();

        // Take top level categories
        cTree = categories.stream().filter( category -> category.getParent() == null ).collect(Collectors.toMap( Category::getId, category -> category ) );

        // Fill categories with child ones
        List<Category> children = categories.stream().filter( category -> category.getParent() != null ).collect(Collectors.toList());
        children = children.stream().sorted( new Category.CategoryComparator() ).collect(Collectors.toList());

        Iterator<UUID> it = cTree.keySet().iterator();
        while( it.hasNext() )
        {
            UUID id = it.next();
            cTree.get( id ).setChild( children.stream().filter( ch -> ch.getParent().equals( id ) ).collect(Collectors.toList()));
        }

        // Make the sorted list
        return cTree.values().stream().sorted( new Category.CategoryComparator() ).collect(Collectors.toList());
    }
}