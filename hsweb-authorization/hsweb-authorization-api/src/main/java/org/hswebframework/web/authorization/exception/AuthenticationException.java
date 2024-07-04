package org.hswebframework.web.authorization.exception;

import lombok.Getter;
import org.hswebframework.web.exception.I18nSupportException;

@Getter
public class AuthenticationException extends I18nSupportException {


    public static String ILLEGAL_PASSWORD = "illegal_password";

    public static String USER_DISABLED = "user_disabled";


    private final String code;

    public AuthenticationException(String code) {
        this(code, "error." + code);
    }

    public AuthenticationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AuthenticationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends AuthenticationException {
        public NoStackTrace(String code) {
            super(code);
        }

        public NoStackTrace(String code, String message) {
            super(code, message);
        }

        public NoStackTrace(String code, String message, Throwable cause) {
            super(code, message, cause);
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
