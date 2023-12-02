package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.max.budgetcontrol.datasource.WidgetOnlineUpdater;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.Arrays;
import java.util.List;

public class OnBootReceiver extends BroadcastReceiver
{
   @Override
   public void onReceive(Context context, Intent intent)
   {
      if (intent != null) {
         if (intent.getAction().equalsIgnoreCase(
                 Intent.ACTION_BOOT_COMPLETED)) {
            updateWidgets(context);
         }
      }
   }

   private void updateWidgets(Context context)
   {
      Log.i( this.getClass().getName(), "[updateWidgets] Update widgets after boot" );
      AppWidgetManager wm = AppWidgetManager.getInstance( context );
      int[] appIds = wm.getAppWidgetIds(new ComponentName(
              context, BCWidget.class ));

      BCDBHelper bcdbHelper = BCDBHelper.getInstance(context);
      ViewMakerFactory factory = new ViewMakerFactory( context );
      List<WidgetParams> widgets = bcdbHelper.getWidgets( appIds );

      for( WidgetParams widget : widgets )
      {
         if(Arrays.stream(appIds).filter(id -> id == widget.getAppId() ).findFirst().isPresent() )
         {
            WidgetOnlineUpdater updater = new WidgetOnlineUpdater( context, wm,
                                                          factory.getViewMaker( 200, widget ), widget );
            updater.updateWidget(null);
            Log.d ( this.getClass().getName(), "[updateWidgets] " + widget.getTitle() + " updated from cash" );
         }
      }
   }
}
