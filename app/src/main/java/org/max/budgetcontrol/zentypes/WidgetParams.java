package org.max.budgetcontrol.zentypes;

import android.graphics.Color;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class WidgetParams
{
    public static final String TITLE = "title";
    public static final String AMOUNT = "amount";
    public static final String PERIOD = "period";

    public static final int INVALID_WIDGET_ID = -1;

    int id;

    int appId;

    double limitAmount;

    StartPeriodEncoding startPeriod;

    public double getCurrentAmount()
    {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount)
    {
        this.currentAmount = currentAmount;
    }

    double currentAmount;

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

    public StartPeriodEncoding getStartPeriod()
    {
        return startPeriod;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    String title;

    public List<UUID> getCategories()
    {
        return categories;
    }

    public void setCategories(List<UUID> categories)
    {
        this.categories = categories;
    }

    List<UUID> categories;

    Map<String, LabelParams> labels;

    public WidgetParams()
    {
        this.limitAmount = 0;
        this.startPeriod = StartPeriodEncoding.month;
        this.id = INVALID_WIDGET_ID;
        this.appId = -1;
        categories = new ArrayList<>();
        labels = new HashMap<>();
        labels.put(TITLE, new LabelParams());
        labels.put(AMOUNT, new LabelParams(Color.valueOf(0xFFD6D6D6), Color.valueOf(Color.BLACK)));
        labels.put(PERIOD, new LabelParams());

        title = "";
    }

    public LabelParams getLabelParams(String partName)
    {
        assert partName != null : "Parameter ca not be null";

        if (Stream.of(TITLE, AMOUNT, PERIOD).noneMatch(partName::equals))
            throw new InvalidParameterException(partName + " is not valid widget part name");
        return labels.get(partName);
    }

    public void setLabelParams(String partName, LabelParams params)
    {
        assert partName != null && params != null : "Parameter can not be null";

        if (Stream.of(TITLE, AMOUNT, PERIOD).noneMatch(partName::equals))
            throw new InvalidParameterException(partName + " is not valid widget part name");
        labels.remove( partName );
        labels.put(partName, params);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setAppId(int appId)
    {
        this.appId = appId;
    }

    public void setLimitAmount(double limitAmount)
    {
        this.limitAmount = limitAmount;
    }

    public void setStartPeriod(StartPeriodEncoding startPeriod)
    {
        this.startPeriod = startPeriod;
    }

    public void addCategoryId(UUID uuid)
    {
        categories.add(uuid);
    }

    public LabelParams getPeriodParams()
    {
        return labels.get(PERIOD);
    }

    public LabelParams getAmountParams()
    {
        return labels.get(AMOUNT);
    }

    public LabelParams getTitleParams()
    {
        return labels.get(TITLE);
    }

    public static class LabelParams
    {
        private Color backColor;
        private Color fontColor;

        public void setBackColor(Color backColor)
        {
            this.backColor = backColor;
        }

        public void setFontColor(Color fontColor)
        {
            this.fontColor = fontColor;
        }

        public void setBackColor(int backColor)
        {
            this.backColor = Color.valueOf(backColor);
        }

        public void setFontColor(int fontColor)
        {
            this.fontColor = Color.valueOf(fontColor);
        }

        public Color getBackColor()
        {
            return backColor;
        }

        public Color getFontColor()
        {
            return fontColor;
        }

        public LabelParams(Color backColor, Color fontColor)
        {
            this.backColor = backColor;
            this.fontColor = fontColor;
        }

        public LabelParams()
        {
            this.backColor = Color.valueOf(Color.WHITE);
            this.fontColor = Color.valueOf(Color.BLACK);
        }
    }
}
