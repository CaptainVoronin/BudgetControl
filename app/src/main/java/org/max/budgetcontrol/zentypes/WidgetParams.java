package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

public class WidgetParams
{

    public static final int INVALID_WIDGET_ID = -1;

    public int id;

    public int appId;

    public double limitAmount;

    public StartPeriodEncoding startPeriod;

    public List<UUID> getCategories()
    {
        return categories;
    }

    public void setCategories(List<UUID> categories)
    {
        this.categories = categories;
    }

    List<UUID> categories;

    public WidgetParams( )
    {
        this.limitAmount = 0;
        this.startPeriod = StartPeriodEncoding.month;
        this.id = INVALID_WIDGET_ID;
        this.appId = -1;
        categories = new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public void setStartPeriod(StartPeriodEncoding startPeriod) {
        this.startPeriod = startPeriod;
    }

}
