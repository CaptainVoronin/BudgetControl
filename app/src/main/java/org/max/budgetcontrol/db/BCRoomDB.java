package org.max.budgetcontrol.db;

import org.max.budgetcontrol.zentypes.BCSetting;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.WidgetCategory;
import org.max.budgetcontrol.zentypes.WidgetParams;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.RawQuery;
import androidx.room.RoomDatabase;

@Dao
@Database(entities = {WidgetParams.class, WidgetCategory.class, BCSetting.class}, version = 1)
public abstract class BCRoomDB  extends RoomDatabase
{
   public abstract BCDao bcDao();
}
