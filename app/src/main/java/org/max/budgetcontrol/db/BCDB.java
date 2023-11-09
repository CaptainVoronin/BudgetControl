package org.max.budgetcontrol.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class BCDB extends SQLiteOpenHelper
{
   // If you change the database schema, you must increment the database version.
   public static final int DATABASE_VERSION = 1;
   public static final String DATABASE_NAME = "bcbd.db";

   public static final String create_categories_table = "create table category ( " +
           "id text not null primary key," +
           "name text not null," +
           "parent_id integer" +
           ");";

   public static final String create_widget_table = "create table widget ( " +
           "id integer primary key autoincrement," +
           "app_id integer not null," +
           "limit_amount real, " +
           "start_period not null" +
           ");";

   public static final String create_widget_cats_table = "create table widget_cats ( " +
           "id integer primary key autoincrement," +
           "widget_id integer not null," +
           "category_id text not null, " +
           "foreign key ( widget_id ) references widget ( id )," +
           "foreign key ( category_id ) references category ( id )" +
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
      db.execSQL( create_categories_table );
      db.execSQL( create_widget_table );
      db.execSQL( create_widget_cats_table );
      db.execSQL( create_settings );
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int i, int i1)
   {
      db.execSQL( "drop table if exists widget_cats" );
      db.execSQL( "drop table if exists widget" );
      db.execSQL( "drop table if exists category" );
      db.execSQL( "delete from settings" );

      db.execSQL( create_categories_table );
      db.execSQL( create_widget_table );
      db.execSQL( create_widget_cats_table );
      db.execSQL( create_settings );
   }
}
