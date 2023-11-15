package org.max.budgetcontrol.zentypes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity( tableName = "widget" )
public class WidgetParams
{
    @PrimaryKey (autoGenerate = true)
    int id;

    @ColumnInfo( name="app_id" )
    int appId;

    @ColumnInfo( name = "limit_amount")
    double limitAmount;

    @ColumnInfo( name = "start_period")
    long startPeriod;

    public int getId()
    {
        return id;
    }

    public int getAppId()
    {
        return appId;
    }

    public double getLimitAmount()
    {
        return limitAmount;
    }

    public long getStartPeriod()
    {
        return startPeriod;
    }
}
