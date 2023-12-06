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
            return new BaseWidgetViewMaker1(context, widget);
         case 401:
         default:
            return new NoConnectionViewMaker( context, widget );
      }
   }

   private AWidgetViewMaker getViewFor200Code(WidgetParams widget) {
      if( widget.getLimitAmount() > 1 )
         return new AmountLimitViewMaker( context, widget );
      else
         return new BaseWidgetViewMaker1(context, widget);
   }
}
