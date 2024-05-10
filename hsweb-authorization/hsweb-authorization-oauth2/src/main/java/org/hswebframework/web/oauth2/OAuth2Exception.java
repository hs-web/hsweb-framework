package org.hswebframework.web.oauth2;

import lombok.Getter;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;

@Getter
public class OAuth2Exception extends BusinessException {
    private final ErrorType type;

    public OAuth2Exception(ErrorType type) {
        super(type.message(), type.name(), type.code(), (Object[]) null);
        this.type = type;
    }

    public OAuth2Exception(String message, Throwable cause, ErrorType type) {
        super(message, cause);
        this.type = type;
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends OAuth2Exception {
        public NoStackTrace(ErrorType type) {
            super(type);
        }

        public NoStackTrace(String message, Throwable cause, ErrorType type) {
            super(message, cause, type);
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
