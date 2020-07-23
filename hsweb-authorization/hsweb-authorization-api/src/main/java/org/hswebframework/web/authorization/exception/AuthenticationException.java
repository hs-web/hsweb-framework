package org.hswebframework.web.authorization.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {


    public static String ILLEGAL_PASSWORD = "illegal_password";

    public static String USER_DISABLED = "user_disabled";


    private final String code;

    public AuthenticationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AuthenticationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
