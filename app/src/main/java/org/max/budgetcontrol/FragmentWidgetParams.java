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

import org.max.budgetcontrol.zentypes.StartPeriodEncoding;
import org.max.budgetcontrol.zentypes.WidgetParams;

public class FragmentWidgetParams extends ABCFragment {

    View fragmentView;

    StartPeriodEncoding currentPeriodCode;

    ViewPager viewPager;

    EditText edTitle;

    EditText edAmount;

    ValueChangeListener titleListener;

    ValueChangeListener amountListener;

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

        configSpinner(getMainActivity().getCurrentWidget());

        bringWidgetToUI();

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       /* viewPager = view.findViewById( R.id.pager);
        BCPagerAdapter bcp = new BCPagerAdapter( getChildFragmentManager(), getMainActivity() );
        viewPager.setAdapter( bcp );*/
    }

    private void bringWidgetToUI() {
        edTitle = fragmentView.findViewById(R.id.edTitle);
        edTitle.setText(getMainActivity().getCurrentWidget().getTitle());

        configSpinner(getMainActivity().getCurrentWidget());

        edAmount = fragmentView.findViewById(R.id.edAmount);
        edAmount.setText("" + getMainActivity().getCurrentWidget().getLimitAmount());

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

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener) {
        super.setParamsStateListener(paramsStateListener);
        titleListener = new ValueChangeListener(edTitle, paramsStateListener, value -> {
            if (value == null)
                return false;
            return value.toString().trim().length() > 0;
        }) {
            @Override
            protected void valueChanged(boolean checkResult) {
                paramsStateListener.setTitleComplete(checkResult);
            }
        };

        amountListener = new ValueChangeListener(edAmount, paramsStateListener, value -> {
            if (value == null)
                return false;
            if (value.toString().trim().length() == 0) return false;

            double amount = -1;
            try {
                amount = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }

            return amount >= 0;
        }) {
            @Override
            protected void valueChanged(boolean checkResult) {
                paramsStateListener.setAmountLimitComplete(checkResult);
            }
        };

        String buff = edAmount.getText().toString();
        try {
            double d = Double.parseDouble(buff);
            paramsStateListener.setAmountLimitComplete(d >= 0);
        } catch (NumberFormatException e) {
            paramsStateListener.setAmountLimitComplete(false);
        }

        buff = edTitle.getText().toString();
        paramsStateListener.setTitleComplete(buff.trim().length() > 0);
    }

    @Override
    public void applyEnteredValues() {
        TextView tv = fragmentView.findViewById(R.id.edTitle);
        getMainActivity().getCurrentWidget().setTitle(tv.getText().toString());
        tv = fragmentView.findViewById(R.id.edAmount);
        String buff = tv.getText().toString();
        double val = Double.parseDouble(buff);
        getMainActivity().getCurrentWidget().setLimitAmount(val);
        getMainActivity().getCurrentWidget().setStartPeriod(currentPeriodCode);
    }
}