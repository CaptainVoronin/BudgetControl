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
    StartPeriodEncoding startPeriod;

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

    public void setStartPeriod(String startPeriod) {
        this.startPeriod = StartPeriodEncoding.valueOf( startPeriod );
    }

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

    public String getStartPeriod()
    {
        return startPeriod.toString();
    }

    public StartPeriodEncoding getStartPeriodCode()
    {
        return startPeriod;
    }
}
