package org.max.preditor;

import android.content.Context;
import android.text.InputType;

import org.max.preditor.editors.IPropertyEditor;
import org.max.preditor.editors.ITypeConverter;
import org.max.preditor.editors.PropertyEditorSimpleType;

import java.util.List;

public class PropertyAdapterDouble extends APropertyAdapter<Double>
{
    public PropertyAdapterDouble(Context context, int layoutId, String key, String header, Double value, List<Object> items, int default_value_index)
    {
        super(context, layoutId, key, header, value, items, default_value_index);
    }

    @Override
    public ITypeConverter<Double> getTypeConverter()
    {
        return value -> {

            if (value == null) return 0d;
            if (value.toString().trim().length() == 0) return 0d;
            Double result;
            try
            {
                result = Double.valueOf(value.toString());
            } catch (NumberFormatException e)
            {
                result = 0d;
            }
            return result;
        };
    }

    @Override
    public IPropertyEditor<Double> getPropertyEditor(Context context)
    {
        return new PropertyEditorSimpleType<>(context, getTypeConverter(), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }


}