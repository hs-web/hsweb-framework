package org.hswebframework.web.authorization.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
public enum DefaultDataAccessType implements DataAccessType, EnumDict<String> {
    USER_OWN_DATA("自己的数据"),
    FIELD_DENY("禁止操作字段"),
    DIMENSION_SCOPE("维度范围");

    private final String name;

    @Override
    public String getText() {
        return name;
    }

    @Override
    public String getValue() {
        return getId();
    }

    @Override
    public String getId() {
        return name().toLowerCase();
    }

}
