package org.hswebframework.web.bean;

import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

import java.util.Collection;

@Getter
public class ClassDescription {
    private final Class<?> type;

    private final boolean collectionType;
    private final boolean arrayType;
    private final boolean enumType;
    private final boolean enumDict;
    private final int fieldSize;

    private final Object[] enums;

    public ClassDescription(Class<?> type) {
        this.type = type;
        collectionType = Collection.class.isAssignableFrom(type);
        enumDict = EnumDict.class.isAssignableFrom(type);
        arrayType = type.isArray();
        enumType = type.isEnum();
        fieldSize = type.getDeclaredFields().length;
        if (enumType) {
            enums = type.getEnumConstants();
        } else {
            enums = null;
        }
    }

}
