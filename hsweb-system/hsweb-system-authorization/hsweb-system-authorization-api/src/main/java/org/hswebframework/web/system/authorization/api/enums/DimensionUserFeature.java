package org.hswebframework.web.system.authorization.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
public enum DimensionUserFeature implements EnumDict<String> {
    mergeChildrenPermission("合并子级维度权限")
    ;

    private final String text;

    @Override
    public String getValue() {
        return name();
    }
}
