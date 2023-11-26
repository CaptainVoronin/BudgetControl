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

   public IWidgetViewMaker getViewMaker(WidgetParams widget )
   {
      return new BaseWidgetViewMaker( context, widget );
   }
}
