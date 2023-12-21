package org.max.budgetcontrol;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.max.budgetcontrol.zentypes.WidgetParams;

public class FragmentWidgetAppearance extends ABCFragment
{

    TextView tvTitle;
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
        View rootView = inflater.inflate(R.layout.fragment_widget_appearance, container, false);

        tvTitle = rootView.findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(new TextViewClickListener(WidgetParams.TITLE));

        tvAmount = rootView.findViewById(R.id.tvAmount);
        tvAmount.setOnClickListener(new TextViewClickListener(WidgetParams.AMOUNT));

        tvPeriod = rootView.findViewById(R.id.tvPeriodName);
        tvPeriod.setOnClickListener(new TextViewClickListener(WidgetParams.PERIOD));

        return rootView;
    }

    private void editColors(String widgetPartName, View v)
    {
        TextView tv = (TextView) v;
        WidgetParams.LabelParams labelParams = getMainActivity().getCurrentWidget().getLabelParams(widgetPartName);
        getColors( labelParams, widgetPartName );
    }

    private void getColors(WidgetParams.LabelParams currentParams, String widgetPart )
    {
        DlgColorSelector dialog = new DlgColorSelector( getMainActivity(),
                                                        currentParams,
                                                        new ColorSelectedListener(widgetPart),
                                                        "" );
        dialog.show();
    }

    int getColor()
    {
        return Color.GREEN;
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

    }

    class ColorSelectedListener implements  DlgColorSelector.OnOkListener
    {
        private final String widgetPart;

        public ColorSelectedListener(String widgetPart )
        {
            this.widgetPart = widgetPart;
        }
        @Override
        public void okPressed(WidgetParams.LabelParams newLabelParams)
        {
            TextView tv = getSpecificTextView( widgetPart );
            tv.setBackgroundColor( newLabelParams.getBackColor().toArgb() );
            tv.setTextColor( newLabelParams.getFontColor().toArgb() );
        }
    }

    private TextView getSpecificTextView(String widgetPart)
    {
        int id = 0;
        if( widgetPart.equals( WidgetParams.TITLE ))
            return tvTitle;
        else if ( widgetPart.equals( WidgetParams.AMOUNT ))
            return tvAmount;
        else
            return tvPeriod;
    }

    class TextViewClickListener implements View.OnClickListener
    {
        String widgetPart;
        public TextViewClickListener( String widgetPart )
        {
            this.widgetPart = widgetPart;
        }
        @Override
        public void onClick(View view)
        {
            editColors( widgetPart, view );
        }
    }
}