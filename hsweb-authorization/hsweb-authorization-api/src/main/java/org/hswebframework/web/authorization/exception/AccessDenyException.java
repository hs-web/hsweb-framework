package org.hswebframework.web.authorization.exception;

/**
 * 权限验证异常
 *
 * @author zhouhao
 * @since 3.0
 */
public class AccessDenyException extends RuntimeException {

    public AccessDenyException() {
        this("{access_deny}");
    }

    public AccessDenyException(String message) {
        super(message);
    }

    public AccessDenyException(String message, Throwable cause) {
        super(message, cause);
    }
}
