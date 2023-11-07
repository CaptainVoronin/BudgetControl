package org.max.budgetcontrol.datasource;

public enum ZenEntities {

    instrument ("instrument"),
    company ("company"),
    user ("user"),
    account ("account"),
    tag ("tag"),
    merchant ("merchant"),
    budget ("budget"),
    reminder ("reminder"),
    reminderMarker ("reminderMarker"),
    transaction ( "transaction" );

    private String name;

    ZenEntities( String name )
    {
       this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

}
