package org.max.budgetcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class WidgetCategoryHolder {
    List<UUID> cats;
    WidgetParamsStateListener paramsStateListener;

   /*public boolean isChanged()
   {
      return isChanged;
   }*/

    boolean isChanged;

    public WidgetCategoryHolder(WidgetParamsStateListener paramsStateListener, List<UUID> cats) {
        assert paramsStateListener != null : "ParamsStateListener can not be null";
        assert cats != null : "Category list can not be null";

        this.cats = new ArrayList<>();
        this.cats.addAll(cats);
        this.paramsStateListener = paramsStateListener;
        isChanged = false;
        this.paramsStateListener.setCategoriesComplete(cats.size() != 0);
    }

    public void add(List<UUID> uuidList) {
        assert uuidList != null : "Category id can not be null";
        List<UUID> toAdd = uuidList.stream().filter( selected-> !cats.contains( selected ) ).collect(Collectors.toList());
        cats.addAll( toAdd );
        isChanged = cats.size() != 0;
        paramsStateListener.setCategoriesComplete(isChanged);
    }

    public void remove(List<UUID> uuidList) {
        assert uuidList != null : "Category id can not be null";
        List<UUID> toRemove = uuidList.stream().filter( selected-> cats.contains( selected ) ).collect(Collectors.toList());
        cats.removeAll( toRemove );
        isChanged = cats.size() != 0;
        paramsStateListener.setCategoriesComplete(isChanged);
    }

}