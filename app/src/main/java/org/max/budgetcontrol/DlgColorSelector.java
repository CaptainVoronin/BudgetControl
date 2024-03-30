package org.max.budgetcontrol;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import net.mm2d.color.chooser.ColorChooserDialog;

import org.max.budgetcontrol.zentypes.WidgetParams;
import org.max.preditor.editors.ColorDialog;

import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

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

    boolean applyToWholeWidget;

    public DlgColorSelector(MainActivity mainActivity, WidgetParams.LabelParams currentParams, OnOkListener listener, String title)
    {
        this.mainActivity = mainActivity;
        this.listener = listener;
        newTextColor = currentParams.getFontColor().toArgb();
        newBgColor = currentParams.getBackColor().toArgb();
        this.title = title;
        applyToWholeWidget = false;
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
            UUID key = UUID.randomUUID();
            ColorChooserDialog dlg = ColorChooserDialog.INSTANCE;
            dlg.show(mainActivity, key.toString(), newTextColor, false, 0, new int[]{0});
            dlg.registerListener(mainActivity,
                    key.toString(),
                    (Function1<? super Integer, Unit>) value -> {
                        newTextColor = value;
                        GradientDrawable drw = (GradientDrawable) imvText.getBackground();
                        drw.setColor(newTextColor);
                        return null;
                    },
                    (Function0<Unit>) () -> null);
        });

        GradientDrawable drw = (GradientDrawable) imvText.getBackground();
        drw.setColor(newTextColor);

        ImageView imvBack = v.findViewById(R.id.imageViewColorBackground);
        imvBack.setOnClickListener(v1 -> {
            UUID key = UUID.randomUUID();
            ColorChooserDialog dlg = ColorChooserDialog.INSTANCE;
            dlg.show(mainActivity, key.toString(), newBgColor, false, 0, new int[]{0});
            dlg.registerListener(mainActivity,
                    key.toString(),
                    (Function1<? super Integer, Unit>) value -> {
                        newBgColor = value;
                        GradientDrawable drw1 = (GradientDrawable) imvBack.getBackground();
                        drw1.setColor(newBgColor);
                        return null;
                    },
                    (Function0<Unit>) () -> null);
        });

        drw = (GradientDrawable) imvBack.getBackground();
        drw.setColor(newBgColor);

        builder.setView(v);

        builder.setPositiveButton(android.R.string.ok, (dialog, i) -> {
            WidgetParams.LabelParams params = new WidgetParams.LabelParams(Color.valueOf(newBgColor),
                    Color.valueOf(newTextColor));
            listener.okPressed(params, applyToWholeWidget);
        });

        CheckBox cb = v.findViewById( R.id.cbToWholeWidget );
        cb.setOnCheckedChangeListener((compoundButton, b) -> {
            applyToWholeWidget = b;
        });

        builder.show();
    }

    public interface OnOkListener
    {
        void okPressed(WidgetParams.LabelParams newLabelParams, boolean applyToWholeWidget );
    }
}
