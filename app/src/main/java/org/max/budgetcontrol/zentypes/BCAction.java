package org.max.budgetcontrol.zentypes;

public abstract class BCAction
{
    BCAction nextAction;

    public void exec()
    {
        _exec();
        if( nextAction != null )
            nextAction.exec();
    };

    abstract protected void _exec();
}
