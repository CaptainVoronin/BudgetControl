package org.max.budgetcontrol.db;

import org.max.budgetcontrol.zentypes.BCSetting;
import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.WidgetParams;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.RawQuery;
import androidx.room.RoomDatabase;


public abstract class BCRoomDB  extends RoomDatabase
{
   public abstract BCRoomDB userDao();
}
