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
    private final boolean arrayType;
    private final boolean enumType;
    private final boolean enumDict;
    private final int fieldSize;
    private final boolean number;

    private final Object[] enums;
    private final Map<String, Field> fields;

    public ClassDescription(Class<?> type) {
        this.type = type;
        collectionType = Collection.class.isAssignableFrom(type);
        enumDict = EnumDict.class.isAssignableFrom(type);
        arrayType = type.isArray();
        enumType = type.isEnum();
        fieldSize = type.getDeclaredFields().length;
        number = Number.class.isAssignableFrom(type);
        if (enumType) {
            enums = type.getEnumConstants();
        } else {
            enums = null;
        }
        fields = Arrays
            .stream(type.getDeclaredFields())
            .collect(Collectors.toMap(Field::getName, f -> f, (a, b) -> b));
    }

}
