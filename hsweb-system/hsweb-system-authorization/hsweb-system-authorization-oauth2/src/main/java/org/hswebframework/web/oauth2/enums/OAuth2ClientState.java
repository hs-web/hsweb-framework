package org.hswebframework.web.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
public enum OAuth2ClientState implements EnumDict<String> {

    enabled("启用"),
    disabled("禁用");
    private final String text;

    @Override
    public String getValue() {
        return name();
    }

}
