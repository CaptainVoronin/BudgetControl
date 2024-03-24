package org.max.preditor;

public interface IItemChecker<T extends Object> {
    T check(Object item );
}
