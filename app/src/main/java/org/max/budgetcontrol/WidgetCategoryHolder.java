package org.max.budgetcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void add(UUID uuid) {
        assert uuid != null : "Category id can not be null";

        if (!cats.contains(uuid)) {
            cats.add(uuid);
            isChanged = true;
            paramsStateListener.setCategoriesComplete(true);
        }
    }

    public void remove(UUID uuid) {
        assert uuid != null : "Category id can not be null";
        if (cats.contains(uuid)) {
            cats.remove(uuid);
            isChanged = true;
            paramsStateListener.setCategoriesComplete(cats.size() != 0);
        }
    }
}