package org.max.budgetcontrol.db;

import org.max.budgetcontrol.zentypes.BCSetting;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.WidgetParams;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Category.class,
                      BCSetting.class,
                      WidgetParams.class },
        version = 1)
public abstract class BCRoomDB  extends RoomDatabase
{
   public abstract BCRoomDB userDao();
}
