package org.max.preditor.editors;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

public class ColorDialog {
    private final Context context;
    private String title;
    private int color;
    private OnClickListener positiveListener;

    AlertDialog dialog;

    ImageView imageView;

    Button colorButton;

    GradientDrawable background;

    public ColorDialog(Context context) {
        this.context = context;
    }

    public ColorDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ColorDialog setSelectedColor(int color) {
        this.color = color;
        return this;
    }

    public ColorDialog show() {
        dialog = build();
        dialog.show();
        return this;
    }

    private AlertDialog build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null)
            builder.setTitle(title);
        builder.setView(getView());
        builder.setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setPositiveButton(android.R.string.ok, (dialog, i) -> {
            if (positiveListener != null)
                positiveListener.onButtonClick(getSelectedColor());
            dialog.dismiss();

        });

        return builder.create();
    }

    private View getView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(org.max.preditor.R.layout.color_dialog, null);
        ColorPicker colorPicker = view.findViewById(org.max.preditor.R.id.colorPicker);
        colorPicker.setColor(color);
        imageView = view.findViewById(org.max.preditor.R.id.imageView);
        background = (GradientDrawable) imageView.getBackground();
        colorButton = view.findViewById(org.max.preditor.R.id.randomColorButton);
        innerSetColor( color );
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                ColorDialog.this.color = color;
                innerSetColor(color);
            }
        });
        return view;
    }

    public ColorDialog setPositiveButtonListener(OnClickListener listener) {
        this.positiveListener = listener;
        return this;
    }

    public interface OnClickListener {
        void onButtonClick(int selectedColor);
    }

    public int getSelectedColor() {
        return color;
    }

    private void innerSetColor(int c) {

        background.setColor(c);
        colorButton.setBackgroundColor(c);
        String hexColor = String.format("#%06X", (0xFFFFFF & c));
        colorButton.setText(hexColor);
    }
}