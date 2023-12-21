package org.max.budgetcontrol;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.max.budgetcontrol.zentypes.WidgetParams;

import androidx.appcompat.app.AlertDialog;

class DlgColorSelector
{
    private final WidgetParams.LabelParams currentParams;
    private final String title;
    MainActivity mainActivity;

    private OnOkListener listener;

    GetSingleColorListener textColorListener;
    GetSingleColorListener bgColorListener;

    ValueHolder<Integer> textColorHolder;
    ValueHolder<Integer> bgColorHolder;

    int textColor;
    int bgColor;


    public DlgColorSelector(MainActivity mainActivity, WidgetParams.LabelParams currentParams, OnOkListener listener, String title)
    {
        this.mainActivity = mainActivity;
        this.currentParams = currentParams;
        this.listener = listener;
        textColor = currentParams.getFontColor().toArgb();
        bgColor = currentParams.getBackColor().toArgb();
        this.title = title;
    }

    public void show()
    {
        textColorHolder = new ValueHolder<>(textColor);
        bgColorHolder = new ValueHolder<>(bgColor);

        textColorListener = new GetSingleColorListener(textColorHolder);
        bgColorListener = new GetSingleColorListener(bgColorHolder);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setTitle( title );

        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dlg_color_set, null);

        ImageView imv = v.findViewById(R.id.imageViewColorText);
        imv.setOnClickListener(textColorListener);

        GradientDrawable drw = (GradientDrawable) imv.getBackground();
        drw.setColor(textColor);

        imv = v.findViewById(R.id.imageViewColorBackground);
        imv.setOnClickListener(bgColorListener);

        drw = (GradientDrawable) imv.getBackground();
        drw.setColor(bgColor);

        builder.setView(v);

        builder.setPositiveButton(android.R.string.ok, (dialog, i) -> {
            WidgetParams.LabelParams newParams =
                    new WidgetParams.LabelParams(Color.valueOf(bgColorHolder.getValue()), Color.valueOf(textColorHolder.getValue()));
            listener.okPressed(newParams);
        });
        builder.show();
    }

    public interface OnOkListener
    {
        void okPressed(WidgetParams.LabelParams newLabelParams);
    }

    class GetSingleColorListener implements View.OnClickListener
    {
        ValueHolder<Integer> valueHolder;

        GetSingleColorListener(ValueHolder<Integer> vHolder)
        {
            this.valueHolder = vHolder;
        }

        @Override
        public void onClick(View v)
        {
            valueHolder.setValue( Color.RED );
        }
    }

    class ValueHolder<T>
    {
        T value;

        public ValueHolder(T value)
        {
            this.value = value;
        }

        public void setValue(T value)
        {
            this.value = value;
        }

        public T getValue()
        {
            return value;
        }
    }
}
