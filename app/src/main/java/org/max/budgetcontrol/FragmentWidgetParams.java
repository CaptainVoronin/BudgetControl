package org.max.budgetcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.preditor.IPropertyAdapter;
import org.max.preditor.PropertySet;


public class FragmentWidgetParams extends ABCFragment {

    View fragmentView;

    StartPeriodEncoding currentPeriodCode;

    ViewPager viewPager;

    ValueChangeListener titleListener;

    ValueChangeListener amountListener;

    PropertySet propertyHolder;

    public FragmentWidgetParams(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public String getTitle() {
        return getMainActivity().getString(R.string.tab_title_common_settings);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_widget_params, container, false);
        propertyHolder = new PropertySet(getContext(), fragmentView.findViewById(R.id.layoutWidgetParams), R.raw.widget_props_form, R.layout.widget_property);
        try {
            propertyHolder.createViews();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        bringWidgetToUI();
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void bringWidgetToUI() {
        IPropertyAdapter<String> pes = (IPropertyAdapter<String>) propertyHolder.getByKey( "title" );
        pes.setValue( getMainActivity().getCurrentWidget().getTitle() );

        IPropertyAdapter<Double> ped = (IPropertyAdapter<Double>) propertyHolder.getByKey( "limit_amount" );
        ped.setValue( getMainActivity().getCurrentWidget().getLimitAmount() );

        pes = (IPropertyAdapter<String>) propertyHolder.getByKey( "period" );
        pes.setSelectedItemIndex( getMainActivity().getCurrentWidget().getStartPeriod().number() );

    }

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener) {

        IPropertyAdapter<String> pes = (IPropertyAdapter<String>) propertyHolder.getByKey( "title" );
        pes.setChangeListener(s -> paramsStateListener.setTitleComplete( s != null && s.trim().length() != 0 ));

        pes.setBeforeChangeListener( (value)->{
            if( value != null && value.toString().trim().length() >0 )
                return true;
            else
                return false;
        });

        paramsStateListener.setTitleComplete( pes.getValue() != null
                                              && pes.getValue().trim().length() != 0 );

        IPropertyAdapter<Double> ped = (IPropertyAdapter<Double>) propertyHolder.getByKey( "limit_amount" );
        ped.setBeforeChangeListener(
                (value)-> {
                    if( value == null ) return false;
                    try{
                        Double.parseDouble( value.toString() );
                        return true;
                    }
                    catch ( NumberFormatException e )
                    {
                        return false;
                    }
                }
        );
        ped.setChangeListener( value-> paramsStateListener.setAmountLimitComplete( true ));
        double val = ped.getValue();
        paramsStateListener.setAmountLimitComplete( true );
    }

    @Override
    public void applyEnteredValues() {
        IPropertyAdapter<String> pes = (IPropertyAdapter<String>) propertyHolder.getByKey( "title");

        getMainActivity().getCurrentWidget().setTitle(pes.getValue());

        IPropertyAdapter<Double> ped = (IPropertyAdapter<Double>) propertyHolder.getByKey( "limit_amount");
        getMainActivity().getCurrentWidget().setLimitAmount(ped.getValue());

        pes = (IPropertyAdapter<String>) propertyHolder.getByKey( "period");
        String buff = pes.getValue();
        StartPeriodEncoding code = null;
        if( "неделя".equals( buff ) ) code = StartPeriodEncoding.week;
        else if( "месяц".equals( buff ) ) code = StartPeriodEncoding.month;
        else if( "год".equals( buff ) ) code = StartPeriodEncoding.year;

        getMainActivity().getCurrentWidget().setStartPeriod(code);
    }

    @Override
    public void onSettingsComplete()
    {

    }
}