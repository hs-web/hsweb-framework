package org.hswebframework.web.oauth2;

import lombok.Getter;
import org.hswebframework.web.exception.BusinessException;

@Getter
public class OAuth2Exception extends BusinessException {
    private final ErrorType type;

    public OAuth2Exception(ErrorType type) {
        super(type.message(), type.name(), type.code());
        this.type = type;
    }

    public OAuth2Exception(String message, Throwable cause, ErrorType type) {
        super(message, cause);
        this.type = type;
    }
}
