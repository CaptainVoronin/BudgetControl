package org.max.preditor.editors;

import android.content.Context;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class PropertyEditorSimpleType<T> extends APropertyEditor<T> {
    int inputType;

    public PropertyEditorSimpleType(Context context, ITypeConverter<T> converter, int inputType) {
        super(context, converter);
        this.inputType = inputType;
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getTitle())
                .setNegativeButton(android.R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.cancel());

        if (getValues() != null) {
            builder.setSingleChoiceItems(getValues().stream().map(item -> item.toString()).toArray(CharSequence[]::new),
                    getDefaultValueIndex(),
                    (dialog, i) -> {
                        if (getListener() != null)
                            getListener().onOkPressed(getValues().get(i));
                        dialog.cancel();
                    });
        } else {
            EditText input = getEditor();
            if (getValue() != null)
                input.setText(getValue().toString());

            builder.setPositiveButton(android.R.string.ok, ((dialog, i) -> {
                if (getListener() != null)
                    getListener().onOkPressed(getTypeConverter().convertValue(input.getText().toString()));
                dialog.cancel();
            }));
            builder.setView(input);
        }

        builder.show();
    }

    protected EditText getEditor() {
        EditText editText = new EditText(getContext());
        editText.setInputType(inputType);
        return editText;
    }
}
