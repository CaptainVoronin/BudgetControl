package org.max.budgetcontrol.zentypes;

import org.max.budgetcontrol.datasource.ZenEntities;

import java.util.UUID;

public interface IZenType
{
   UUID getId();

   String getTitle();

   ZenEntities getType();

   Integer getUserId();

   UnixTimestamp changed();

   UnixTimestamp created();
}
