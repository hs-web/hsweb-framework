package org.hswebframework.web.system.authorization.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
@Dict("user-state")
public enum UserState implements EnumDict<String> {
    enabled("正常"), disabled("已禁用");

    private String text;

    @Override
    public String getValue() {
        return name();
    }


}
