package org.max.budgetcontrol;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.max.budgetcontrol.zentypes.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CategoryListViewAdapter extends ArrayAdapter<Category> implements CompoundButton.OnCheckedChangeListener
{
    List<UUID> selectedCats;

    Context context;

    FragmentCategories fragmentCategories;
    Map<UUID, Category> flatMap;

    public CategoryListViewAdapter(Context context, FragmentCategories fragmentCategories, List<Category> flatList, List<UUID> widgetCts)
    {
        super(context, R.layout.category_list_item, flatList);
        this.context = context;
        flatMap = new HashMap<>();
        flatList.stream().forEach( item->flatMap.put( item.getId(), item ) );

        this.fragmentCategories = fragmentCategories;

        if (widgetCts != null)
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
        TextView tv = (TextView) view.findViewById(R.id.tvItemName);
        tv.setText(category.getName());

        CheckBox cb = (CheckBox) view.findViewById(R.id.cbSelected);
        cb.setTag(category);
        if (selectedCats != null && selectedCats.size() != 0)
        {
            if (selectedCats.contains(category.getId()))
                cb.setChecked(true);
            else
                cb.setChecked(false);
        } else
            cb.setChecked(false);

        cb.setOnCheckedChangeListener(this);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
    {
        Category category = (Category) compoundButton.getTag();

        Predicate<Category> findAbsentCat = cat -> !selectedCats.contains(cat.getId());
        Predicate<Category> findPresentCat = cat -> selectedCats.contains(cat.getId());
        Predicate<Category> actualFilter;

        Consumer<Category> addCat = cat -> selectedCats.add(cat.getId());
        Consumer<Category> removeCat = cat -> selectedCats.remove(cat.getId());
        Consumer<Category> actualAction;

        if (checked)
        {
            if (!selectedCats.contains(category.getId()))
                selectedCats.add(category.getId());
            actualFilter = findAbsentCat;
            actualAction = addCat;
        } else
        {
            selectedCats.remove(category.getId());
            actualFilter = findPresentCat;
            actualAction = removeCat;
        }

        category.getChild().stream().filter(actualFilter).forEach(actualAction);
        notifyDataSetChanged();
        fragmentCategories.setSelectedCategoriesList( selectedCats, checked );
    }
}
