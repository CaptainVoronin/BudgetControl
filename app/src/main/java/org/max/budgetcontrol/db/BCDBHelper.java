package org.max.budgetcontrol.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BCDBHelper
{
   Context context;
   BCDB bcdb;
   SQLiteDatabase db;

   public BCDBHelper( Context context )
   {
      this.context = context;
   }

   public void open()
   {
      bcdb = new BCDB( context );
      db = bcdb.getWritableDatabase();
   }

   public WidgetParams loadWidgetParamsByAppId( Integer appId )
   {
      WidgetParams wp = null;
      String queryWidget = "select id, limit_amount, start_period from widget where app_id = ?";
      String queryCats = "select category_id from widget_cats where widget_id = ?";

      Cursor crs = db.rawQuery( queryWidget, new String[]{ appId.toString() } );

      if( crs.getCount() != 0 )
      {
         crs.moveToNext();
         int wID = crs.getInt(0);
         double limit = crs.getDouble(1);
         String buff = crs.getString(2);
         wp = new WidgetParams();
         wp.setAppId( appId );
         wp.setId( wID );
         wp.setStartPeriod( StartPeriodEncoding.valueOf( buff ) );
         crs.close();

         crs = db.rawQuery( queryCats, new String[]{ Integer.toString( wID ) } );
         List<UUID> cats = new ArrayList<>();

         while( crs.moveToNext() )
         {
            buff = crs.getString( 0 );
            cats.add( UUID.fromString( buff ) );
         }
         wp.setCategories( cats );
      }
      return wp;
   }

   public long insertWidgetParams( WidgetParams wp )
   {
      ContentValues cv = new ContentValues();

      cv.put( "app_id", wp.appId );
      cv.put( "limit_amount", wp.limitAmount );
      cv.put( "start_period", wp.startPeriod.toString() );

      db.beginTransaction();

      long id = db.insert( BCDB.TABLE_WIDGET, null, cv );
      cv.clear();

      List<UUID> cats = wp.getCategories();

      for ( UUID uuid : cats )
      {
         cv.put( "widget_id", id );
         cv.put( "category_id", uuid.toString() );
         db.insert( BCDB.TABLE_WIDGET_CATS, null, cv );
      }

      db.endTransaction();

      return id;
   }

   public void updateWidgetParams( WidgetParams wp )
   {
      ContentValues cv = new ContentValues();

      cv.put( "limit_amount", wp.limitAmount );
      cv.put( "start_period", wp.startPeriod.toString() );

      db.beginTransaction();

      db.update( BCDB.TABLE_WIDGET, cv, "id=" + wp.id, null );
      cv.clear();

      List<UUID> cats = wp.getCategories();

      db.delete( BCDB.TABLE_WIDGET_CATS, "widget_id=?", new String[]{ Integer.toString( wp.id )});

      for ( UUID uuid : cats )
      {
         cv.put( "widget_id", wp.id );
         cv.put( "category_id", uuid.toString() );
         db.insert( BCDB.TABLE_WIDGET_CATS, null, cv );
      }

      db.endTransaction();
   }

   protected void onDestroy()
   {
      db.close();
      bcdb.close();
   }

}
