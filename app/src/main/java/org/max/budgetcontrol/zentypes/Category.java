package org.max.budgetcontrol.zentypes;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.ZenEntities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class Category extends AZenType implements Serializable
{

    public boolean isOutcome()
    {
        return outcome;
    }

    public UUID getId()
    {
        return id;
    }

    /**
     * @return Chile list. May be empty but can n't be null
     */
    public @NonNull List<Category> getChild()
    {
        return child;
    }

    public void setChild(List<Category> child)
    {
        this.child = child;
    }

    List<Category> child;

    boolean outcome;
    UUID parent;

    public UUID getParent()
    {
        return parent;
    }

    protected Category(UUID id, String title, boolean outcome, String parent, Long changed)
    {
        super(id, title, ZenEntities.tag, 0, new UnixTimestamp(changed), new UnixTimestamp(changed));
        this.outcome = outcome;
        if (parent != null)
            this.parent = UUID.fromString(parent);
        else
            this.parent = null;
        child = new ArrayList<>();
    }

    public Category()
    {
        child = new ArrayList<>();
    }

    public boolean addChild(Category c)
    {
        if (!c.getParent().equals(getId()))
            return false;
        else
        {
            if (!child.contains(c)) child.add(c);
            return true;
        }
    }

    public static Category fromJSONObject(JSONObject obj) throws JSONException
    {
        boolean b = obj.getBoolean("showOutcome");
        String title = obj.getString("title");
        String parent = null;
        String buff = obj.getString("id");
        UUID id = UUID.fromString(buff);
        Long changed = obj.getLong("changed");
        if (!obj.isNull("parent"))
            parent = obj.getString("parent");
        return new Category(id, title, b, parent, changed);
    }

    public static class CategoryComparator implements Comparator<Category>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Category a, Category b)
        {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    }
}
