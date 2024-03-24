package org.max.preditor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.max.preditor.editors.IPropertyEditor;

import java.util.List;
import java.util.stream.Collectors;

public abstract class APropertyAdapter<T> implements IPropertyAdapter<T> {

    private final Context context;
    private List<T> items;
    private final int default_value_index;
    private T value;

    public int getLayoutId()
    {
        return layoutId;
    }

    private int layoutId;

    private String key;

    private String header;

    private String prompt;

    public IValidator getValidator()
    {
        return validator;
    }

    private IValidator validator;

    View view;

    public IPropertyChangeListener<T> getChangeListener()
    {
        return changeListener;
    }

    private IPropertyChangeListener<T> changeListener;

    IPropertyEditor<T> customEditor;

    public APropertyAdapter(Context context,
                            int layoutId,
                            String key,
                            String header,
                            T value,
                            List<Object> items,
                            int default_value_index) {
        this.context = context;
        this.layoutId = layoutId;
        this.key = key;
        this.header = header;
        this.value = value;
        if (items != null)
            this.items = items.stream().map(item -> getTypeConverter().convertValue(item)).collect(Collectors.toList());

        this.default_value_index = default_value_index;
        customEditor = null;
    }

    @Override
    public View getView() {
        if( view == null )
        {
            //LayoutInflater inflater = LayoutInflater.from(getContext());
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutId, null);
            TextView tv = view.findViewWithTag(PROPERTY_HEADER_TAG);
            tv.setText(header);
            tv = view.findViewWithTag(PROPERTY_VALUE_TAG);
            if (value != null)
                tv.setText(value.toString());

            tv.setOnClickListener(this);
        }
        return view;
    }

    @Override
    public final T getValue() {
        return value;
    }

    @Override
    public final void setBeforeChangeListener(IValidator validator) {
        this.validator = validator;
    }

    @Override
    public final void setChangeListener(IPropertyChangeListener<T> changeListener) {
        this.changeListener = changeListener;
    }

    public final Context getContext() {
        return context;
    }

    public final String getKey() {
        return key;
    }

    @Override
    public final void onClick(View v) {
        editValue();
    }

    @Override
    public boolean setValue(T newValue) {
        boolean result = false;
        if (validator != null) {
            if (validator.isValid(newValue)) {
                value = newValue;
                result = true;
            }
        } else {
            value = newValue;
            result = true;
        }
        if (result) {
            if (changeListener != null)
                changeListener.onChanged(value);
            TextView tv = view.findViewWithTag(PROPERTY_VALUE_TAG);
            if (tv != null)
                tv.setText(value.toString());
        }
        return true;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public final List<T> getItems() {
        return items;
    }

    public int getDefault_value_index() {
        return default_value_index;
    }

    @Override
    public void editValue() {
        int index = IPropertyAdapter.INVALID_DEFAULT_VALUE_INDEX;
        if (getValue() != null) {
            if (getItems() != null)
                for (int i = 0; i < getItems().size(); i++)
                    if (getValue().equals(getItems().get(i))) {
                        index = i;
                        break;
                    }
        } else if (getDefault_value_index() != IPropertyAdapter.INVALID_DEFAULT_VALUE_INDEX)
            index = getDefault_value_index();

        IPropertyEditor<T> editor;
        if (customEditor == null)
            editor = getPropertyEditor(context);
        else
            editor = customEditor;

        editor.setTitle(getHeader());

        if (getItems() != null)
            editor.setValues(getItems(), index);

        editor.setValue(getValue());
        editor.setOnOkListener(v -> setValue(getTypeConverter().convertValue(v)));
        editor.show();
    }

    @Override
    public void setSelectedItemIndex(int index) {
        T value = items.get(index);
        setValue(value);
    }

    @Override
    public void setCustomPropertyEditor(IPropertyEditor<T> editor) {
        this.customEditor = editor;
    }
}
