package org.max.budgetcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class WidgetCategoryHolder
{
   List<UUID> cats;

   public boolean isChanged()
   {
      return isChanged;
   }

   boolean isChanged;

   public WidgetCategoryHolder(List<UUID> cats )
   {
      this.cats = new ArrayList<>();
      this.cats.addAll( cats );
      isChanged = false;
   }

   public void add( UUID uuid )
   {
      if( !cats.contains( uuid ) )
      {
         cats.add( uuid );
         isChanged = true;
      }
   }

   public void remove( UUID uuid )
   {
      if( cats.contains( uuid ) )
      {
         cats.remove( uuid );
         isChanged = true;
      }
   }
}
