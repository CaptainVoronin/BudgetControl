package org.max.budgetcontrol;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.preditor.editors.ColorDialog;

import androidx.appcompat.app.AlertDialog;

class DlgColorSelector
{
    private final String title;
    MainActivity mainActivity;

    private OnOkListener listener;

/*
    GetSingleColorListener textColorListener;
    GetSingleColorListener bgColorListener;

    ValueHolder<Integer> textColorHolder;
    ValueHolder<Integer> bgColorHolder;

*/
    int newTextColor;
    int newBgColor;


    public DlgColorSelector(MainActivity mainActivity, WidgetParams.LabelParams currentParams, OnOkListener listener, String title)
    {
        this.mainActivity = mainActivity;
        this.listener = listener;
        newTextColor = currentParams.getFontColor().toArgb();
        newBgColor = currentParams.getBackColor().toArgb();
        this.title = title;
    }

    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setTitle(title);

        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg_color_set, null);

        ImageView imvText = v.findViewById(R.id.imageViewColorText);
        imvText.setOnClickListener(v1 -> {
            ColorDialog dialog = new ColorDialog(mainActivity);
            dialog.setSelectedColor(newTextColor);
            dialog.setPositiveButtonListener(new ColorDialog.OnClickListener()
            {
                @Override
                public void onButtonClick(int selectedColor)
                {
                    newTextColor = selectedColor;
                    GradientDrawable drw = (GradientDrawable) imvText.getBackground();
                    drw.setColor(newTextColor);
                }
            });
            dialog.show();
        });

        GradientDrawable drw = (GradientDrawable) imvText.getBackground();
        drw.setColor(newTextColor);

        ImageView imvBack = v.findViewById(R.id.imageViewColorBackground);
        imvBack.setOnClickListener(v1 -> {
            ColorDialog dialog = new ColorDialog(mainActivity);
            dialog.setSelectedColor(newBgColor);
            dialog.setPositiveButtonListener(new ColorDialog.OnClickListener()
            {
                @Override
                public void onButtonClick(int selectedColor)
                {
                    newBgColor = selectedColor;
                    GradientDrawable drw = (GradientDrawable) imvBack.getBackground();
                    drw.setColor(newBgColor);
                }
            });
            dialog.show();
        });

        drw = (GradientDrawable) imvBack.getBackground();
        drw.setColor(newBgColor);

        builder.setView(v);

        builder.setPositiveButton(android.R.string.ok, (dialog, i) -> {
            WidgetParams.LabelParams params = new WidgetParams.LabelParams( Color.valueOf(newBgColor),
                    Color.valueOf(newTextColor) );
            listener.okPressed(params);
        });
        builder.show();
    }

    public interface OnOkListener
    {
        void okPressed(WidgetParams.LabelParams newLabelParams);
    }
}
