package org.hswebframework.web.bean;

import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

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
        this.number = Number.class.isAssignableFrom(type);
        if (enumType) {
            this.enums = type.getEnumConstants();
        } else {
            this.enums = null;
        }
        Map<String, Field> fields = new HashMap<>();
        ReflectionUtils.doWithFields(type, field -> fields.put(field.getName(), field));
        this.fields = Collections.unmodifiableMap(fields);
        this.fieldSize = this.fields.size();
    }

}
