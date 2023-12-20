package org.max.budgetcontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BCPagerAdapter extends FragmentPagerAdapter {

    List<ABCFragment> fragments;
    MainActivity mainActivity;
    public BCPagerAdapter(@NonNull FragmentManager fm, MainActivity mainActivity ) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mainActivity = mainActivity;
        fragments = new ArrayList<>(2);
        fragments.add( new FragmentWidgetParams(mainActivity) );
        fragments.add( new FragmentCategories(mainActivity) );
        fragments.add( new FragmentWidgetAppearance(mainActivity) );
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get( position );
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTitle();
    }
}
