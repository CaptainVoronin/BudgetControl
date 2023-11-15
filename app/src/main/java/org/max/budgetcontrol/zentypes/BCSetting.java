package org.max.budgetcontrol.zentypes;


import org.jetbrains.annotations.PropertyKey;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity( tableName = "settings")
public class BCSetting
{
   @PrimaryKey
   int id;

   @ColumnInfo( name = "name")
   String name;

   @ColumnInfo( name = "value" )
   String value;

}
