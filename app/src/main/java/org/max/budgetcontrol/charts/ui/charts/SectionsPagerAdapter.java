package org.max.budgetcontrol.charts.ui.charts;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.charts.ChartActivity;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter
{

    public static final int CHART_FRAGMENT_INDEX = 0;
    public static final int TRANSACTION_FRAGMENT_INDEX = 1;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_chart_header, R.string.tab_transactions_header};
    private final Context mContext;

    ChartFragment chartInstance;
    TransactionFragment transactionsInstance;

    public SectionsPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position)
    {
        if( position == 0)
        {
            if( chartInstance == null )
                chartInstance= ChartFragment.newInstance((ChartActivity) mContext, CHART_FRAGMENT_INDEX);
            return chartInstance;
        }
        else
        {
            if( transactionsInstance == null )
                transactionsInstance = TransactionFragment.newInstance((ChartActivity) mContext, TRANSACTION_FRAGMENT_INDEX);
            return transactionsInstance;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount()
    {
        // Show 2 total pages.
        return 2;
    }
}