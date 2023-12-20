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

public class FragmentWidgetAppearance extends ABCFragment {

    TextView tvTitle;
    TextView tvAmount;
    TextView tvPeriod;

    public FragmentWidgetAppearance(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_widget_appearance, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(new TextViewClickListener(WidgetParams.TITLE));

        tvAmount = view.findViewById(R.id.tvAmount);
        tvAmount.setOnClickListener(new TextViewClickListener(WidgetParams.AMOUNT));

        tvPeriod = view.findViewById(R.id.tvPeriodName);
        tvPeriod.setOnClickListener(new TextViewClickListener(WidgetParams.PERIOD));

        return view;
    }

    private void editColors(String widgetPartName, View v) {
        TextView tv = (TextView) v;
        WidgetParams.LabelParams currentParams = getMainActivity().getCurrentWidget().getLabelParams(widgetPartName);

        WidgetParams.LabelParams params = getColors(currentParams);
        tv.setBackgroundColor( params.getBackColor().toArgb() );
        tv.setTextColor(params.getFontColor().toArgb());

        getMainActivity().getCurrentWidget().setLabelParams(widgetPartName, params);
    }

    class GetSingleColorListener implements View.OnClickListener {
        int color;

        GetSingleColorListener(int color) {
            this.color = color;
        }

        @Override
        public void onClick(View v) {

        }

        int getColor() {
            return color;
        }
    }

    class StupidListener implements DialogInterface.OnClickListener {
        boolean fired = false;

        boolean isFired() {
            return fired;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            fired = true;
            dialog.cancel();
        }
    }

    private WidgetParams.LabelParams getColors(WidgetParams.LabelParams currentParams) {
        WidgetParams.LabelParams labelParams = currentParams;
        int textColor = currentParams.getFontColor().toArgb();
        int bgColor = currentParams.getBackColor().toArgb();
        StupidListener positiveStupid = new StupidListener();

        GetSingleColorListener textColorListener = new GetSingleColorListener(textColor);
        GetSingleColorListener bgColorListener = new GetSingleColorListener(bgColor);
        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.cancel());

        LayoutInflater inflater = getMainActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg_color_set, null);

        ImageView imv = v.findViewById(R.id.imageViewColorText);
        imv.setOnClickListener(textColorListener);

        GradientDrawable drw = (GradientDrawable) imv.getBackground();
        drw.setColor( textColor );

        imv = v.findViewById(R.id.imageViewColorBackground);
        imv.setOnClickListener(bgColorListener);
        drw = (GradientDrawable) imv.getBackground();
        drw.setColor( bgColor );

        builder.setView(v);

        builder.setPositiveButton(android.R.string.ok, positiveStupid);
        builder.show();

        if (positiveStupid.isFired())
            labelParams = new WidgetParams.
                    LabelParams(Color.valueOf(textColorListener.getColor()),
                    Color.valueOf(bgColorListener.getColor()));

        return labelParams;
    }

    int getColor() {
        return Color.GREEN;
    }

    @Override
    public String getTitle() {
        return getMainActivity().getString(R.string.fragment_appearance_title);
    }

    @Override
    public void initListeners(WidgetParamsStateListener paramsStateListener) {

    }

    @Override
    public void applyEnteredValues() {

    }

    class TextViewClickListener implements View.OnClickListener {
        String widgetPartName;

        public TextViewClickListener(String widgetPartName) {
            this.widgetPartName = widgetPartName;
        }

        @Override
        public void onClick(View v) {
            editColors(widgetPartName, v);
        }
    }
}