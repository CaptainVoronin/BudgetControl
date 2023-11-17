package org.max.budgetcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;

import org.max.budgetcontrol.zentypes.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CategoryListViewAdapter extends BaseExpandableListAdapter {
    List<Category> categories;
    Map<UUID, Category> widgetCats;

    Context context;

    public CategoryListViewAdapter(Context context, List<Category> categories) {
        this.categories = categories;
        this.context = context;
        widgetCats = null;
    }

    public CategoryListViewAdapter(Context context, List<Category> categories, List<Category> widgetCts) {
        this.categories = categories;
        this.context = context;
        widgetCats = new HashMap<>();

        makeWidgetFlatCatList(widgetCats, widgetCts);
    }

    private void makeWidgetFlatCatList(Map<UUID, Category> cts, List<Category> widgetCts) {
        for (Category c : widgetCts) {
            cts.put(c.getId(), c);
            if (c.getChild().size() != 0)
                makeWidgetFlatCatList(cts, c.getChild());
        }
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return categories.get(i).getChild().size();
    }

    @Override
    public Object getGroup(int i) {
        return categories.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return categories.get(i).getChild().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Category category = categories.get(i);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.category_list_group_item, null);
            CheckedTextView tv = (CheckedTextView) view.findViewById(R.id.tvItemName);
            tv.setText(category.getName());
            tv.setTag(category.getId());
            if (widgetCats != null) {
                if (widgetCats.containsKey(category.getId()))
                    tv.setChecked(true);
            }
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        Category child = categories.get(i).getChild().get(i1);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.category_list_item, null);

            CheckedTextView tv = (CheckedTextView) view.findViewById(R.id.tvItemName);
            tv.setText(child.getName());
            tv.setTag(child.getId());
            if (widgetCats != null) {
                if (widgetCats.containsKey(child.getId()))
                    tv.setChecked(true);
            }
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
