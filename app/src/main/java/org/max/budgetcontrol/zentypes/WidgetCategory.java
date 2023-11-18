package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

@Entity(tableName = "widget_cats",
        foreignKeys = {
                @ForeignKey(entity = WidgetParams.class,
                        parentColumns = {"id"},
                        childColumns = {"widget_id"},
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        }
)
public class WidgetCategory
{
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NotNull
    @ColumnInfo(name = "category_id")
    public UUID categoryId;

    @NotNull
    @ColumnInfo(name = "widget_id")
    public int widgetId;

    @Ignore
    public int getId()
    {
        return id;
    }

    @Ignore
    public void setId(int id)
    {
        this.id = id;
    }

    @TypeConverter
    public static String fromUUID( UUID id )
    {
        return id.toString();
    }

    @TypeConverter
    public static UUID toUUID(@NotNull String categoryId)
    {
        return UUID.fromString(categoryId);
    }

    @Ignore
    public UUID getCategoryUUID()
    {
        return categoryId;
    }
    @Ignore
    public int getWidgetId()
    {
        return widgetId;
    }
    @Ignore
    public void setWidgetId(int widgetId)
    {
        this.widgetId = widgetId;
    }
}
