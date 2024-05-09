package org.max.budgetcontrol.zentypes;

import java.util.Date;

public class UnixTimestamp implements Comparable<UnixTimestamp>
{
    long valueMills;
    long valueSec;

    public UnixTimestamp(long value)
    {
        initValues(value);
    }

    public UnixTimestamp()
    {
        initValues(System.currentTimeMillis());
    }

    public UnixTimestamp(Date date)
    {
        initValues(date.getTime());
    }

    private void initValues(long value)
    {
        if (value < 10000000000l)
        {
            valueSec = value;
            valueMills = value * 1000L;
        } else
        {
            valueSec = value / 1000L;
            valueMills = value;
        }
    }

    public long mills()
    {
        return valueMills;
    }

    public long sec()
    {
        return valueSec;
    }

    @Override
    public int compareTo(UnixTimestamp value)
    {
        if (mills() == value.mills())
            return 0;
        else if (mills() > value.mills())
            return 1;
        else
            return -1;
    }

    public static int compare(UnixTimestamp v1, UnixTimestamp v2)
    {
        return v1.compareTo(v2);
    }

}
