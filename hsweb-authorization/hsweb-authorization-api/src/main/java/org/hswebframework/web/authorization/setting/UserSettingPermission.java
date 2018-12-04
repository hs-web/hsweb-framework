package org.hswebframework.web.authorization.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@AllArgsConstructor
@Getter
@Dict(id = "user-setting-permission")
public enum UserSettingPermission implements EnumDict<String> {
    NONE("无"),
    R("读"),
    W("写"),
    RW("读写");
    private String text;

    @Override
    public String getValue() {
        return name();
    }
}
