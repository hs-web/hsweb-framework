package org.hswebframework.web.utils;

import lombok.AllArgsConstructor;

import java.lang.reflect.Array;
import java.util.AbstractList;

@AllArgsConstructor
public class DynamicArrayList<E> extends AbstractList<E> {

    private final Object value;

    @Override
    public E get(int index) {
        return (E) Array.get(value, index);
    }

    @Override
    public int size() {
        return Array.getLength(value);
    }
}
