package org.max.budgetcontrol;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public abstract class ABCFragment extends Fragment implements MainActivity.SettingsCompleteListener
{
    public WidgetParamsStateListener getParamsStateListener()
    {
        return paramsStateListener;
    }

    public void setParamsStateListener(WidgetParamsStateListener paramsStateListener)
    {
        this.paramsStateListener = paramsStateListener;
    }


    private WidgetParamsStateListener paramsStateListener;

    public MainActivity getMainActivity()
    {
        return mainActivity;
    }

    private MainActivity mainActivity;

    public ABCFragment(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    public abstract String getTitle();

    public abstract void initListeners(WidgetParamsStateListener item);

    public abstract void applyEnteredValues();

}
