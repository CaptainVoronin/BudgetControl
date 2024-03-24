package org.max.preditor;

import android.content.Context;
import android.text.InputType;

import org.max.preditor.editors.IPropertyEditor;
import org.max.preditor.editors.ITypeConverter;
import org.max.preditor.editors.PropertyEditorSimpleType;

import java.util.List;

public class PropertyAdapterInteger extends APropertyAdapter<Integer> {
    public PropertyAdapterInteger(Context context, int layoutId, String key, String header, Integer value, List<Object> items, int default_value_index) {
        super(context, layoutId, key, header, value, items, default_value_index);
    }

    @Override
    public ITypeConverter<Integer> getTypeConverter() {
        return value -> {
            if (value == null) return 0;
            if (value.toString().trim().length() == 0) return 0;
            Integer result;
            try
            {
                result = Integer.valueOf(value.toString());
            } catch (NumberFormatException e)
            {
                result = new Integer(0);
            }
            return result;
        };
    }

    @Override
    public IPropertyEditor<Integer> getPropertyEditor(Context context)
    {
        return new PropertyEditorSimpleType<>(context, getTypeConverter(), InputType.TYPE_CLASS_NUMBER);
    }
}