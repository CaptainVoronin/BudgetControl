package org.max.preditor.editors;

import android.content.Context;

import java.util.List;

public abstract class APropertyEditor<T> implements IPropertyEditor<T>
{
    private final Context context;
    private T value;

    private String title;
    private int defaultValueIndex;

    private IOnOkListener listener;

    List<T> values;

    ITypeConverter<T> typeConverter;

    public APropertyEditor(Context context, ITypeConverter<T> typeConverter)
    {
        this.context = context;
        this.typeConverter = typeConverter;
        values = null;
    }

    public final Context getContext()
    {
        return context;
    }

    public final T getValue()
    {
        return value;
    }

    public final List<T> getValues()
    {
        return values;
    }

    public final String getTitle()
    {
        return title;
    }

    public final int getDefaultValueIndex()
    {
        return defaultValueIndex;
    }

    public final IOnOkListener getListener()
    {
        return listener;
    }

    @Override
    public final IPropertyEditor<T> setValue(T value)
    {
        this.value = value;
        return this;
    }

    @Override
    public final IPropertyEditor<T> setTitle(String title)
    {
        this.title = title;
        return this;
    }

    @Override
    public final IPropertyEditor<T> setValues(List<T> values, int defaultValueIndex)
    {
        this.defaultValueIndex = defaultValueIndex;
        this.values = values;
        return this;
    }

    @Override
    public final IPropertyEditor<T> setOnOkListener(IOnOkListener listener)
    {
        this.listener = listener;
        return this;
    }

    @Override
    public ITypeConverter<T> getTypeConverter()
    {
        return typeConverter;
    }
}