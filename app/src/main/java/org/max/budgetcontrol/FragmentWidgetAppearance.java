package org.max.budgetcontrol;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.max.budgetcontrol.zentypes.WidgetParams;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FragmentWidgetAppearance extends ABCFragment
{

    TextView tvTitle;
    WidgetParams.LabelParams titleLabel;
    WidgetParams.LabelParams amountLabel;
    WidgetParams.LabelParams periodLabel;
    TextView tvAmount;
    TextView tvPeriod;

    public FragmentWidgetAppearance(MainActivity activity)
    {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        WidgetParams wp = getMainActivity().getCurrentWidget();

        View rootView = inflater.inflate(R.layout.fragment_widget_appearance, container, false);

        tvTitle = rootView.findViewById(R.id.tvTitle);
        titleLabel = wp.getLabelParams(WidgetParams.TITLE);
        tvTitle.setTextColor(titleLabel.getFontColor().toArgb());
        tvTitle.setBackgroundColor(titleLabel.getBackColor().toArgb());
        tvTitle.setOnClickListener(view -> {
            DlgColorSelector dialog = new DlgColorSelector(getMainActivity(),
                    titleLabel,
                    new ColorSelectedListener(WidgetParams.TITLE),
                    "");
            dialog.show();
        });

        tvAmount = rootView.findViewById(R.id.tvAmount);
        amountLabel = wp.getLabelParams(WidgetParams.AMOUNT);
        tvAmount.setTextColor(amountLabel.getFontColor().toArgb());
        tvAmount.setBackgroundColor(amountLabel.getBackColor().toArgb());
        tvAmount.setOnClickListener(view -> {
            DlgColorSelector dialog = new DlgColorSelector(getMainActivity(),
                    amountLabel,
                    new ColorSelectedListener(WidgetParams.AMOUNT),
                    "");
            dialog.show();
        });

        tvPeriod = rootView.findViewById(R.id.tvPeriodName);
        periodLabel = wp.getLabelParams(WidgetParams.PERIOD);
        tvPeriod.setTextColor(periodLabel.getFontColor().toArgb());
        tvPeriod.setBackgroundColor(periodLabel.getBackColor().toArgb());
        tvPeriod.setOnClickListener(view -> {
            DlgColorSelector dialog = new DlgColorSelector(getMainActivity(),
                    periodLabel,
                    new ColorSelectedListener(WidgetParams.PERIOD),
                    "");
            dialog.show();
        });

        return rootView;
    }

    @Override
    public String getTitle()
    {
        return getMainActivity().getString(R.string.fragment_appearance_title);
    }

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener)
    {

    }

    @Override
    public void applyEnteredValues()
    {
        WidgetParams wp = getMainActivity().getCurrentWidget();
        wp.setLabelParams(WidgetParams.TITLE, titleLabel);
        wp.setLabelParams(WidgetParams.AMOUNT, amountLabel);
        wp.setLabelParams(WidgetParams.PERIOD, periodLabel);
    }

    @Override
    public void onSettingsComplete()
    {

    }

    class ColorSelectedListener implements DlgColorSelector.OnOkListener
    {
        private final String widgetPart;

        public ColorSelectedListener(String widgetPart)
        {
            this.widgetPart = widgetPart;
        }

        @Override
        public void okPressed(WidgetParams.LabelParams newLabelParams, boolean applyToWholeWidget)
        {
            if( !applyToWholeWidget )
            {
                TextView tv = getSpecificTextView(widgetPart);
                tv.setBackgroundColor(newLabelParams.getBackColor().toArgb());
                tv.setTextColor(newLabelParams.getFontColor().toArgb());
                setSpecificLabelParams(widgetPart, newLabelParams);
            }
            else {
                Stream.of(WidgetParams.TITLE, WidgetParams.AMOUNT, WidgetParams.PERIOD)
                        .forEach( item->{
                            TextView tv = getSpecificTextView(item);
                            tv.setBackgroundColor(newLabelParams.getBackColor().toArgb());
                            tv.setTextColor(newLabelParams.getFontColor().toArgb());
                            setSpecificLabelParams(item, newLabelParams);
                        } );
            }
        }
    }

    private void setSpecificLabelParams(String widgetPart, WidgetParams.LabelParams newLabelParams)
    {
        if (widgetPart.equals(WidgetParams.TITLE))
            titleLabel = newLabelParams;
        else if (widgetPart.equals(WidgetParams.AMOUNT))
            amountLabel = newLabelParams;
        else
            periodLabel = newLabelParams;
    }

    private TextView getSpecificTextView(String widgetPart)
    {
        if (widgetPart.equals(WidgetParams.TITLE))
            return tvTitle;
        else if (widgetPart.equals(WidgetParams.AMOUNT))
            return tvAmount;
        else
            return tvPeriod;
    }
}