package org.max.budgetcontrol;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.max.budgetcontrol.datasource.IWidgetUpdater;
import org.max.budgetcontrol.datasource.WidgetUpdaterFactory;
import org.max.budgetcontrol.db.BCDBHelper;
import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.ArrayList;
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
      AppWidgetManager wm = AppWidgetManager.getInstance( context );
      int[] appIds = wm.getAppWidgetIds(new ComponentName(
              context, MainActivity.class
      ));
      WidgetUpdaterFactory factory = new WidgetUpdaterFactory( context, wm, WidgetUpdaterFactory.UpdateType.cash );
      BCDBHelper bcdbHelper = new BCDBHelper(context);
      bcdbHelper.open();
      List<WidgetParams> widgets = bcdbHelper.getWidgets( appIds );

      for( WidgetParams widget : widgets )
      {
         if(Arrays.stream(appIds).filter(id -> id == widget.getAppId() ).findFirst().isPresent() )
         {
            IWidgetUpdater updater = factory.getInstance(widget);
            updater.updateWidget(null);
         }
      }
   }
}
