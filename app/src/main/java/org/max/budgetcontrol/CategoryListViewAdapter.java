package org.max.budgetcontrol;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.max.budgetcontrol.zentypes.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryListViewAdapter extends ArrayAdapter<Category>
{
    List<UUID> selectedCats;

    Context context;

    MainActivity setChangeListener;

    public CategoryListViewAdapter(Context context, MainActivity setChangeListener, List<Category> flatList, List<UUID> widgetCts)
    {
        super(context, R.layout.category_list_item, flatList );
        this.context = context;

        this.setChangeListener = setChangeListener;

        if( widgetCts != null )
            selectedCats = widgetCts;
        else
            selectedCats = new ArrayList<>();
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
        return super.getItem( i );
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
        return super.getItem( i ).getParent() == null ? 0 : 1;
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
            view = infalInflater.inflate( resourceId, null);
        }
        TextView tv = (TextView) view.findViewById(R.id.tvItemName);
        tv.setText(category.getName());

        CheckBox cb = (CheckBox) view.findViewById(R.id.cbSelected);
        cb.setTag(category.getId());
        if (selectedCats != null && selectedCats.size() != 0 )
        {
            if (selectedCats.contains(category.getId()))
                cb.setChecked(true);
        } else
            cb.setChecked(false);

        cb.setOnCheckedChangeListener(setChangeListener);
        return view;
    }

    List<UUID> getSelected()
    {
        return selectedCats;
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
