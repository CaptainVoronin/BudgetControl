package org.max.budgetcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.peditor.IPropertyChangeListener;
import org.max.peditor.IPropertyEditor;
import org.max.peditor.PropertyHolder;


public class FragmentWidgetParams extends ABCFragment {

    View fragmentView;

    StartPeriodEncoding currentPeriodCode;

    ViewPager viewPager;

    ValueChangeListener titleListener;

    ValueChangeListener amountListener;

    PropertyHolder propertyHolder;

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
        propertyHolder = new PropertyHolder(getContext(), fragmentView.findViewById(R.id.layoutWidgetParams), R.raw.widget_props_form, R.layout.widget_property);
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
        IPropertyEditor<String> pes = (IPropertyEditor<String>) propertyHolder.getByKey( "title" );
        pes.setValue( getMainActivity().getCurrentWidget().getTitle() );

        IPropertyEditor<Double> ped = (IPropertyEditor<Double>) propertyHolder.getByKey( "limit_amount" );
        ped.setValue( getMainActivity().getCurrentWidget().getLimitAmount() );

        pes = (IPropertyEditor<String>) propertyHolder.getByKey( "period" );
        pes.setSelectedItemIndex( getMainActivity().getCurrentWidget().getStartPeriod().number() );

    }

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener) {

        IPropertyEditor<String> pes = (IPropertyEditor<String>) propertyHolder.getByKey( "title" );
        pes.setChangeListener(s -> paramsStateListener.setTitleComplete( true ));

        pes.setBeforeChangeListener( (value)->{
            if( value != null && value.trim().length() >0 )
                return true;
            else
                return false;
        });

        IPropertyEditor<Double> ped = (IPropertyEditor<Double>) propertyHolder.getByKey( "limit_amount" );
        ped.setBeforeChangeListener( (value)-> value >= 0);
        ped.setChangeListener( value-> paramsStateListener.setAmountLimitComplete( true ));
    }

    @Override
    public void applyEnteredValues() {
        IPropertyEditor<String> pes = (IPropertyEditor<String>) propertyHolder.getByKey( "title");

        getMainActivity().getCurrentWidget().setTitle(pes.getValue());

        IPropertyEditor<Double> ped = (IPropertyEditor<Double>) propertyHolder.getByKey( "limit_amount");
        getMainActivity().getCurrentWidget().setLimitAmount(ped.getValue());

        pes = (IPropertyEditor<String>) propertyHolder.getByKey( "period");
        String buff = pes.getValue();
        StartPeriodEncoding code = null;
        if( "неделя".equals( buff ) ) code = StartPeriodEncoding.week;
        else if( "месяц".equals( buff ) ) code = StartPeriodEncoding.month;
        else if( "год".equals( buff ) ) code = StartPeriodEncoding.year;

        getMainActivity().getCurrentWidget().setStartPeriod(code);
    }
}