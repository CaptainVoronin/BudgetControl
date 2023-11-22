package org.max.budgetcontrol.zentypes;

public enum StartPeriodEncoding {

    week ( "week", 0 ),
    month ("month", 1),
    year ( "year", 2 );

    private final int orderNum;

    StartPeriodEncoding(String name, int orderNum )
    {
        this.name = name;
        this.orderNum = orderNum;
    }

    String name;

    @Override
    public String toString()
    {
        return name;
    }

    public int number()
    {
        return orderNum;
    }

}
