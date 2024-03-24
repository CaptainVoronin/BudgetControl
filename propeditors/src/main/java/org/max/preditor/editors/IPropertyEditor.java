package org.max.preditor.editors;

import java.util.List;

public interface IPropertyEditor<T>
{
    IPropertyEditor<T> setValue(T value);

    IPropertyEditor<T> setTitle(String title);

    IPropertyEditor<T> setValues(List<T> values, int defaultVallueIndex );

    void show();

    IPropertyEditor<T> setOnOkListener(IOnOkListener listener);

    ITypeConverter<T> getTypeConverter();
}
