package org.max.preditor.editors;

public interface ITypeConverter<T> {
    T convertValue( Object value );
}
