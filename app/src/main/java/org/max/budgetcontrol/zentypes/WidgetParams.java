package org.max.budgetcontrol.zentypes;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

@Entity( tableName = "widget" )
public class WidgetParams
{
    @PrimaryKey (autoGenerate = true)
    public int id;

    @NotNull
    @ColumnInfo( name="app_id" )
    public int appId;

    @ColumnInfo( name = "limit_amount")
    public double limitAmount;

    @NotNull
    @ColumnInfo( name = "start_period")
    public StartPeriodEncoding startPeriod;

    public List<UUID> getCategories()
    {
        return categories;
    }

    List<UUID> categories;

    public WidgetParams(double limitAmount, @NotNull StartPeriodEncoding startPeriod)
    {
        this.limitAmount = 0;
        this.startPeriod = StartPeriodEncoding.month;
        this.id = -1;
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

    @TypeConverter
    public static StartPeriodEncoding stringToStartPeriod(String startPeriod) {
        return StartPeriodEncoding.valueOf( startPeriod );
    }

    @TypeConverter
    public static String startPeriodToString(StartPeriodEncoding startPeriod) {
        return startPeriod.toString();
    }

}
