package org.max.budgetcontrol;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.max.budgetcontrol.zentypes.WidgetParams;

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
        titleLabel =  wp.getLabelParams( WidgetParams.TITLE );
        tvTitle.setTextColor( wp.getLabelParams( WidgetParams.TITLE ).getFontColor().toArgb() );
        tvTitle.setBackgroundColor(wp.getLabelParams( WidgetParams.TITLE ).getBackColor().toArgb() );
        tvTitle.setOnClickListener(new TextViewClickListener(WidgetParams.TITLE));

        tvAmount = rootView.findViewById(R.id.tvAmount);
        amountLabel =  wp.getLabelParams( WidgetParams.AMOUNT );
        tvAmount.setTextColor( wp.getLabelParams( WidgetParams.AMOUNT ).getFontColor().toArgb() );
        tvAmount.setBackgroundColor(wp.getLabelParams( WidgetParams.AMOUNT ).getBackColor().toArgb() );
        tvAmount.setOnClickListener(new TextViewClickListener(WidgetParams.AMOUNT));

        tvPeriod = rootView.findViewById(R.id.tvPeriodName);
        periodLabel = wp.getLabelParams( WidgetParams.PERIOD );
        tvPeriod.setTextColor( wp.getLabelParams( WidgetParams.PERIOD ).getFontColor().toArgb() );
        tvPeriod.setBackgroundColor(wp.getLabelParams( WidgetParams.PERIOD ).getBackColor().toArgb() );
        tvPeriod.setOnClickListener(new TextViewClickListener(WidgetParams.PERIOD));

        return rootView;
    }

    private void editColors(String widgetPartName, View v)
    {
        WidgetParams.LabelParams labelParams = getMainActivity().getCurrentWidget().getLabelParams(widgetPartName);
        getColors(labelParams, widgetPartName);
    }

    private void getColors(WidgetParams.LabelParams currentParams, String widgetPart)
    {
        DlgColorSelector dialog = new DlgColorSelector(getMainActivity(),
                currentParams,
                new ColorSelectedListener(widgetPart),
                "");
        dialog.show();
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
        wp.setLabelParams( WidgetParams.TITLE, titleLabel );
        wp.setLabelParams( WidgetParams.AMOUNT, amountLabel );
        wp.setLabelParams( WidgetParams.PERIOD, periodLabel );
    }

    class ColorSelectedListener implements DlgColorSelector.OnOkListener
    {
        private final String widgetPart;

        public ColorSelectedListener(String widgetPart)
        {
            this.widgetPart = widgetPart;
        }

        @Override
        public void okPressed(WidgetParams.LabelParams newLabelParams)
        {
            TextView tv = getSpecificTextView(widgetPart);
            tv.setBackgroundColor(newLabelParams.getBackColor().toArgb());
            tv.setTextColor(newLabelParams.getFontColor().toArgb());
            setSpecificLabelParams( widgetPart, newLabelParams );

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

    class TextViewClickListener implements View.OnClickListener
    {
        String widgetPart;

        public TextViewClickListener(String widgetPart)
        {
            this.widgetPart = widgetPart;
        }

        @Override
        public void onClick(View view)
        {
            editColors(widgetPart, view);
        }
    }
}