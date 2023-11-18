package org.max.budgetcontrol.db;

import org.max.budgetcontrol.zentypes.WidgetCategory;
import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.budgetcontrol.zentypes.WidgetWithCategories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface BCDao
{
  @Query("SELECT * FROM widget WHERE id = :wId")
   WidgetParams loadWidgetParams(int wId);


  @Query("SELECT * FROM widget_cats WHERE widget_id = :wId")
   List<WidgetCategory> loadWidgetCategory(int wId);


  @Insert
   long insertWidgetParams( WidgetParams wp );

  @Insert
   long[] insertWidgetCategories( WidgetCategory[] cats );

    @Transaction
    @Query("SELECT * FROM widget where id = :wId")
    public WidgetWithCategories getWidgetWithCategories( int wId );
}
