package org.max.budgetcontrol.zentypes;

public enum StartPeriodEncoding {

    month ("month"),
    week ( "week" ),
    year ( "week" );

    StartPeriodEncoding( String name )
    {
        this.name = name;
    }

    String name;

    @Override
    public String toString()
    {
        return name;
    }

}
