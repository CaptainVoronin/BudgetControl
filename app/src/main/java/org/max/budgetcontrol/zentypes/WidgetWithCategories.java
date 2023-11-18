package org.max.budgetcontrol.zentypes;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class WidgetWithCategories
{
    @Embedded
    public WidgetParams widgetParams;

    @Relation(
            parentColumn = "id",
            entityColumn = "widget_id"
    )
    public List<WidgetCategory> widgetCategories;

}
