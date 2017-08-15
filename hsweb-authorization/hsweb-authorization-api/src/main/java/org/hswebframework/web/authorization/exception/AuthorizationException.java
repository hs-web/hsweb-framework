package org.hswebframework.web.authorization.exception;

/**
 * 权限验证异常
 * @author zhouhao
 */
public class AuthorizationException extends RuntimeException {


    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
