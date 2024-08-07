package org.max.budgetcontrol.charts;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.zentypes.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WidgetCategoryListViewAdapter extends ArrayAdapter<Category>
{

    Context context;

    Map<UUID, Category> flatMap;
    UUID selectedCategory;

    CompoundButton.OnCheckedChangeListener changeListener;

    public WidgetCategoryListViewAdapter(Context context, List<Category> flatList, UUID selected, CompoundButton.OnCheckedChangeListener changeListener)
    {
        super(context, R.layout.category_list_item, flatList);
        this.context = context;
        flatMap = new HashMap<>();
        this.changeListener = changeListener;
        this.selectedCategory = selected;
        flatList.stream().forEach(item -> flatMap.put(item.getId(), item));
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver)
    {
        super.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver)
    {
        super.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public int getCount()
    {
        return super.getCount();
    }

    @Override
    public Category getItem(int i)
    {
        return super.getItem(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public int getItemViewType(int i)
    {
        return super.getItem(i).getParent() == null ? 0 : 1;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        Category category = super.getItem(i);
        int resourceId = category.getParent() == null ? R.layout.category_group_list_item : R.layout.category_list_item;
        if (view == null)
        {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(resourceId, null);
        }
        TextView tv = view.findViewById(R.id.tvItemName);
        tv.setText(category.getTitle());

        CheckBox cb = view.findViewById(R.id.cbSelected);
        cb.setTag(category);
        if (selectedCategory != null)
        {
            if (selectedCategory.equals(category.getId()))
                cb.setChecked(true);
            else
                cb.setChecked(false);
        } else
            cb.setChecked(false);

        cb.setOnCheckedChangeListener(changeListener);
        return view;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(int i)
    {
        return true;
    }
}
