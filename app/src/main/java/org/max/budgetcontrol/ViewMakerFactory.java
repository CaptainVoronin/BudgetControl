package org.max.budgetcontrol;

import android.content.Context;

import org.max.budgetcontrol.zentypes.WidgetParams;

public class ViewMakerFactory
{
   private final Context context;

   public ViewMakerFactory(Context context )
   {
      this.context = context;
   }

   public AWidgetViewMaker getViewMaker(int httpCode, WidgetParams widget )
   {
      switch( httpCode ) {
         case 200:
            return new BaseWidgetViewMaker(context, widget);
         case 401:
         default:
            return new NoConnectionViewMaker( context, widget );
      }
   }
}
