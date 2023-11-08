package org.max.budgetcontrol;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.max.zendatasource.ZenConnection;
import org.max.zendatasource.ZenEntities;
import org.max.zentypes.Category;
import org.max.zentypes.CategoryComparator;
import org.max.zentypes.Transaction;
import org.joda.time.DateTime;


public class Main {

    static String token = "0yteuv8iwTQcJpaBQXJ3XDZ3nnh1RV";

    static String ZenAPIUrl = "https://api.zenmoney.ru/v8/diff";

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, ParseException {

        List<UUID> scope = new ArrayList<>( );
        scope.add( UUID.fromString( "da799632-dea7-4879-ad28-6be81ba55be9"));
        scope.add( UUID.fromString( "06b2c662-10c7-4f67-8e24-5944f06c6fae"));
        scope.add( UUID.fromString( "ad786a44-b944-4b36-b11d-c2257a23f42d"));
        ZenConnection connection = new ZenConnection( new URI( ZenAPIUrl ), token );

        JSONObject jResp = connection.getInitialData();

        List<Category> cats = getTags( jResp );
        List<Transaction> trans = makeRawTransactionList( jResp, cats );

        DateTime prev = new DateTime(  ).minusDays( 10 );
        //trans = trans.stream().filter( t -> t.getDate().getTime() >= prev.toDate().getTime()).toList();

        Double summ = getSumm( trans, prev, scope );
     }

    private static Double getSumm(List<Transaction> trans, DateTime prev, List<UUID> scope) {
        Double result = Double.valueOf( 0l );
        List<Transaction> filtered = trans.stream().filter( t -> t.getDate().getTime() >= prev.toDate().getTime()).toList();
        List<Transaction> dfh = new ArrayList<>();
        for ( UUID id : scope )
        {
            dfh.addAll( filtered.stream().filter( t-> t.hasCategory(id) ).toList() );
        }

        for ( UUID id : scope )
        {
            result = result + filtered.stream().filter( t-> t.hasCategory(id) ).mapToDouble( t->t.getAmount() ).sum();
        }
        return result;
    }

    private static List<Transaction> makeRawTransactionList(JSONObject jResp, List<Category> cats ) throws ParseException {
        JSONArray tArray = jResp.getJSONArray( ZenEntities.transaction.toString() );
        List<Transaction> trans = new ArrayList<>();

        Iterator<Object> it = tArray.iterator();
        while( it.hasNext() ) {
            JSONObject jo = (JSONObject) it.next();
            Transaction t = Transaction.newTransaction( (JSONObject) jo, cats );
            if( t != null )
                trans.add( t );
        }
        return trans.stream().filter( t -> t.getAmount() < 0 ).toList();
    }

    private static List<Category>  getTags(JSONObject jResp) {

        JSONArray tags = jResp.getJSONArray( ZenEntities.tag.toString() );
        List<Category> categories = new ArrayList<>();
        for ( int i = 0; i < tags.length(); i++ )
        {
            JSONObject tag = tags.getJSONObject( i );
            Category c = Category.fromJSONObject( tag );
            categories.add( c );
        }

        return makeCategoryTree( categories );

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
        List<Category> set = cTree.values().stream().sorted( new CategoryComparator() ).toList();

        // Sort child categories
        set.forEach( category -> { if( category.getChild() != null ) category.setChild( category.getChild().stream().sorted( new CategoryComparator() ).toList() ); } );

        return set;
    }
}