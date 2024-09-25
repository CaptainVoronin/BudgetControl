package org.max.budgetcontrol.charts.ui.charts;

import android.content.Context;

import org.max.budgetcontrol.R;
import org.max.budgetcontrol.charts.ChartActivity;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter
{

    public static final int CHART_FRAGMENT_INDEX = 0;
    public static final int TRANSACTION_FRAGMENT_INDEX = 1;

    boolean showCharts;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_chart_header, R.string.tab_transactions_header};
    private final Context mContext;

    ChartFragment chartInstance;
    TransactionFragment transactionsInstance;

    public SectionsPagerAdapter(Context context, FragmentManager fm, boolean showCharts)
    {
        super(fm);
        mContext = context;
        this.showCharts = showCharts;
    }

    @Override
    public Fragment getItem(int position)
    {
        if (!showCharts)
            position = 1;

        if (position == 0)
        {
            if (chartInstance == null)
                chartInstance = ChartFragment.newInstance((ChartActivity) mContext, CHART_FRAGMENT_INDEX);
            return chartInstance;
        } else
        {
            if (transactionsInstance == null)
                transactionsInstance = TransactionFragment.newInstance((ChartActivity) mContext, TRANSACTION_FRAGMENT_INDEX);
            return transactionsInstance;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        if (showCharts)
            return mContext.getResources().getString(TAB_TITLES[position]);
        else
            return mContext.getResources().getString(TAB_TITLES[1]);
    }

    @Override
    public int getCount()
    {
        return showCharts ? 2 : 1;
    }
}