package org.max.budgetcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

public class FragmentWidgetParams extends ABCFragment {

    View fragmentView;

    StartPeriodEncoding currentPeriodCode;

    ViewPager viewPager;

    public FragmentWidgetParams(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public String getTitle() {
        return getMainActivity().getString( R.string.tab_title_common_settings );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        fragmentView = inflater.inflate(R.layout.fragment_widget_params, container, false);

        configSpinner(getMainActivity().getCurrentWidget());

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       /* viewPager = view.findViewById( R.id.pager);
        BCPagerAdapter bcp = new BCPagerAdapter( getChildFragmentManager(), getMainActivity() );
        viewPager.setAdapter( bcp );*/
    }

    void configSpinner(WidgetParams widget) {
        Spinner sp = fragmentView.findViewById(R.id.spStartPeriod);
        sp.setAdapter(new StartPeriodSpinAdapter(getMainActivity(), widget));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object obj = view.getTag();
                currentPeriodCode = (StartPeriodEncoding) obj;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp.setSelection(widget.getStartPeriod().number());
    }
}