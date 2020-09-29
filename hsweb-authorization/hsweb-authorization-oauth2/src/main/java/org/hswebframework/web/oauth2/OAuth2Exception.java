package org.hswebframework.web.oauth2;

import lombok.Getter;

@Getter
public class OAuth2Exception extends RuntimeException {
    private final ErrorType type;

    public OAuth2Exception(ErrorType type) {
        super(type.message());
        this.type = type;
    }

    public OAuth2Exception(String message, Throwable cause, ErrorType type) {
        super(message, cause);
        this.type = type;
    }
}
