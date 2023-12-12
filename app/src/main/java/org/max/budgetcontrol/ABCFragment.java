package org.max.budgetcontrol;

import android.view.MenuItem;

import androidx.fragment.app.Fragment;

public abstract class ABCFragment extends Fragment
{
    public MainActivity getMainActivity() {
        return mainActivity;
    }

    private  MainActivity mainActivity;

    public ABCFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public abstract String getTitle();

    public abstract void initListeners( MenuItem item );

}
