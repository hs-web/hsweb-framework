package org.hswebframework.web.bean;

import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ClassDescription {
    private final Class<?> type;

    private final boolean collectionType;
    private final boolean mapType;
    private final boolean arrayType;
    private final boolean enumType;
    private final boolean enumDict;
    private final int fieldSize;
    private final boolean number;

    private final Object[] enums;
    private final Map<String, Field> fields;

    public ClassDescription(Class<?> type) {
        this.type = type;
        this.collectionType = Collection.class.isAssignableFrom(type);
        this.mapType = Map.class.isAssignableFrom(type);
        this.enumDict = EnumDict.class.isAssignableFrom(type);
        this.arrayType = type.isArray();
        this.enumType = type.isEnum();
        this.fieldSize = type.getDeclaredFields().length;
        this.number = Number.class.isAssignableFrom(type);
        if (enumType) {
            this.enums = type.getEnumConstants();
        } else {
            this.enums = null;
        }
        this.fields = Arrays
                .stream(type.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, f -> f, (a, b) -> b));
    }

}
