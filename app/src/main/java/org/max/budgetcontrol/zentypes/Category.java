package org.max.budgetcontrol.zentypes;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class Category {

    public String getName() {
        return name;
    }

    public boolean isOutcome() {
        return outcome;
    }

    public UUID getId() {
        return id;
    }

    /**
     *
     * @return Chile list. May be empty but can n't be null
     */
    public @NonNull List<Category> getChild() {
        return child;
    }

    public void setChild(List<Category> child) {
        this.child = child;
    }

    List<Category> child;

    String name;
    boolean outcome;

    UUID id;

    UUID parent;

    public UUID getParent() {
        return parent;
    }


    protected Category( String id, String name, boolean outcome, String parent ){
        this.id = UUID.fromString( id );
        this.outcome = outcome;
        this.name = name;
        if( parent != null )
            this.parent = UUID.fromString( parent );
        else
            this.parent = null;
        child = new ArrayList<>();
    }

    public Category()
    {
        child = new ArrayList<>();
    }

    public boolean addChild( Category c )
    {
        if( !c.getParent().equals( getId() ) )
            return false;
        else
        {
            if ( !child.contains( c )) child.add( c );
            return true;
        }
    }

    public static Category fromJSONObject(JSONObject obj ) throws JSONException {
        boolean b = obj.getBoolean( "showOutcome" );
        String name = obj.getString( "title" );
        String parent = null;
        String id = obj.getString( "id" );
        if( !obj.isNull("parent" ) )
            parent = obj.getString( "parent" );
        return new Category(  id, name, b, parent );
    }
    public static class CategoryComparator implements Comparator<Category>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Category a, Category b)
        {
            return a.getName().compareToIgnoreCase( b.getName() );
        }
    }
}
