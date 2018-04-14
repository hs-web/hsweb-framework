package org.hswebframework.web.dao.crud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
public enum DataType implements EnumDict<Byte> {
    TYPE1((byte) 1, "类型1"),
    TYPE2((byte) 2, "类型2"),
    TYPE3((byte) 3, "类型3"),
    TYPE4((byte) 4, "类型4");

    private Byte value;

    private String text;
}
