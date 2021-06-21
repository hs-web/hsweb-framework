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
}
