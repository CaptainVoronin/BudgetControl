package org.max.budgetcontrol.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
      assert db != null : "Database is not opened";

      WidgetParams wp = null;
      String queryWidget = "select id, limit_amount, start_period, title from widget where app_id = ?";
      String queryCats = "select category_id from widget_cats where widget_id = ?";

      Cursor crs = db.rawQuery( queryWidget, new String[]{ appId.toString() } );

      if( crs.getCount() != 0 )
      {
         crs.moveToNext();
         int wID = crs.getInt(0);
         double limit = crs.getDouble(1);
         String strStartPeriod = crs.getString(2);
         String title = crs.getString( 3 );
         wp = new WidgetParams();
         wp.setAppId( appId );
         wp.setId( wID );
         wp.setLimitAmount( limit );
         wp.setStartPeriod( StartPeriodEncoding.valueOf( strStartPeriod ) );
         wp.setTitle( title );
         crs.close();

         crs = db.rawQuery( queryCats, new String[]{ Integer.toString( wID ) } );
         List<UUID> cats = new ArrayList<>();

         while( crs.moveToNext() )
         {
            strStartPeriod = crs.getString( 0 );
            cats.add( UUID.fromString( strStartPeriod ) );
         }
         crs.close();
         wp.setCategories( cats );

      }
      return wp;
   }

   public WidgetParams insertWidgetParams( WidgetParams wp )
   {
      assert db != null : "Database is not opened";
      ContentValues cv = new ContentValues();

      cv.put( "app_id", wp.getAppId() );
      cv.put( "limit_amount", wp.getLimitAmount() );
      cv.put( "start_period", wp.getStartPeriod().toString() );
      cv.put( "title", wp.getTitle() );

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

      db.setTransactionSuccessful();
      db.endTransaction();
      wp.setId( ( int ) id );
      return wp;
   }

   public void updateWidgetParams( WidgetParams wp )
   {
      ContentValues cv = new ContentValues();

      cv.put( "limit_amount", wp.getLimitAmount() );
      cv.put( "start_period", wp.getStartPeriod().toString() );
      cv.put( "title", wp.getTitle() );

      db.beginTransaction();

      db.update( BCDB.TABLE_WIDGET, cv, "id=" + wp.getId(), null );
      cv.clear();

      List<UUID> cats = wp.getCategories();

      db.delete( BCDB.TABLE_WIDGET_CATS, "widget_id=?", new String[]{ Integer.toString( wp.getId() )});

      for ( UUID uuid : cats )
      {
         cv.put( "widget_id", wp.getId() );
         cv.put( "category_id", uuid.toString() );
         db.insert( BCDB.TABLE_WIDGET_CATS, null, cv );
         cv.clear();
      }
      db.setTransactionSuccessful();
      db.endTransaction();
   }

   protected void onDestroy()
   {
      db.close();
      bcdb.close();
   }

   public List<WidgetParams> getAllWidgets()
   {
      assert db != null : "Database is not opened";
      final String queryAllWidgets = "select id, app_id, limit_amount, start_period, title from widget";
      final String queryAllWidgetCats = "select widget_id, category_id from widget_cats";

      Cursor crs = db.rawQuery( queryAllWidgets, null );

      List<WidgetParams> widgets = new ArrayList<>( crs.getCount() );

      while( crs.moveToNext() )
      {
         int wID = crs.getInt(0);
         int appId = crs.getInt( 1 );
         double limit = crs.getDouble(2);
         String strStartPeriod = crs.getString(3);
         String title = crs.getString( 4 );

         WidgetParams wp = new WidgetParams();
         wp.setAppId( appId );
         wp.setId( wID );
         wp.setLimitAmount( limit );
         wp.setStartPeriod( StartPeriodEncoding.valueOf( strStartPeriod ) );
         wp.setTitle( title );
         widgets.add( wp );
      }
      crs.close();

      crs = db.rawQuery( queryAllWidgetCats, null );
      Map<UUID, Integer> cats = new HashMap<>(crs.getCount());

      while( crs.moveToNext() )
      {
         Integer widgetId = crs.getInt( 0 );
         UUID catId = UUID.fromString( crs.getString( 1 ) );

         cats.put( catId, widgetId );
      }
      crs.close();

      for( WidgetParams w : widgets )
      {
         Iterator<UUID> it = cats.keySet().iterator();

         while(it.hasNext())
         {
            UUID uuid = it.next();
            Integer wId = cats.get( uuid );
            if( w.getId() == wId.intValue() ) w.addCategoryId( uuid );
         }
      }

      return widgets;
   }

   public List<WidgetParams> getWidgets( int[] ids)
   {
      assert db != null : "Database is not opened";
      final String queryAllWidgets = "select id, app_id, limit_amount, start_period, title from widget where id in ( ? )";
      final String queryAllWidgetCats = "select widget_id, category_id from widget_cats";

      String[] strIds = new String[ids.length];

      int i = 0;
      for( int id : ids ) {
         strIds[i] = Integer.toString(id);
         i++;
      }

      Cursor crs = db.rawQuery( queryAllWidgets, strIds );

      List<WidgetParams> widgets = new ArrayList<>( crs.getCount() );

      while( crs.moveToNext() )
      {
         int wID = crs.getInt(0);
         int appId = crs.getInt( 1 );
         double limit = crs.getDouble(2);
         String strStartPeriod = crs.getString(3);
         String title = crs.getString( 4 );

         WidgetParams wp = new WidgetParams();
         wp.setAppId( appId );
         wp.setId( wID );
         wp.setLimitAmount( limit );
         wp.setStartPeriod( StartPeriodEncoding.valueOf( strStartPeriod ) );
         wp.setTitle( title );
         widgets.add( wp );
      }
      crs.close();

      crs = db.rawQuery( queryAllWidgetCats, null );
      Map<UUID, Integer> cats = new HashMap<>(crs.getCount());

      while( crs.moveToNext() )
      {
         Integer widgetId = crs.getInt( 0 );
         UUID catId = UUID.fromString( crs.getString( 1 ) );

         cats.put( catId, widgetId );
      }
      crs.close();

      for( WidgetParams w : widgets )
      {
         Iterator<UUID> it = cats.keySet().iterator();

         while(it.hasNext())
         {
            UUID uuid = it.next();
            Integer wId = cats.get( uuid );
            if( w.getId() == wId.intValue() ) w.addCategoryId( uuid );
         }
      }

      return widgets;
   }

   public int deleteLost(List<Integer> lost)
   {
      assert db != null : "Database is not opened";

      String[] strIds = new String[lost.size()];

      for( int i = 0; i < lost.size(); i++  )
         strIds[i] = lost.get(i).toString();

      return db.delete( BCDB.TABLE_WIDGET, "id in ?", strIds);

   }

   //TODO: Обработать исключение в транзакции
   public void clearWidgets()
   {
      assert db != null : "Database is not opened";

      db.beginTransaction();
      db.delete( BCDB.TABLE_WIDGET_CATS, null, null );
      db.delete( BCDB.TABLE_WIDGET, null, null );
      db.setTransactionSuccessful();
      db.endTransaction();

   }
}
