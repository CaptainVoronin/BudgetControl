package org.max.budgetcontrol.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BCDB extends SQLiteOpenHelper
{
   // If you change the database schema, you must increment the database version.
   public static final int DATABASE_VERSION = 3;
   public static final String DATABASE_NAME = "bcbd.db";

   public static final String TABLE_WIDGET = "widget";
   public static final String TABLE_WIDGET_CATS = "widget_cats";

   public static final String create_widget_table = "create table widget ( " +
           "id integer primary key autoincrement," +
           "app_id integer not null unique," +
           "limit_amount real not null default 0, " +
           "start_period not null," +
           "title text not null" +
           ");";

   public static final String create_widget_cats_table = "create table widget_cats ( " +
           "widget_id integer not null," +
           "category_id text not null, " +
           "foreign key ( widget_id ) references widget ( id )" +
           ");";

   public static final String create_settings = "create table settings ( " +
           "id integer primary key autoincrement," +
           "name text not null," +
           "value text not null " +
           ");";

   public BCDB(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db)
   {
      db.execSQL( create_widget_table );
      db.execSQL( create_widget_cats_table );
      db.execSQL( create_settings );
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int i, int i1)
   {
      db.execSQL( "drop table if exists widget_cats" );
      db.execSQL( "drop table if exists widget" );
      //db.execSQL( "delete from settings" );

      db.execSQL( create_widget_table );
      db.execSQL( create_widget_cats_table );
   }
}
