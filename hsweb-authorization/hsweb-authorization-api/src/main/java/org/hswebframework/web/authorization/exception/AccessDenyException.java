package org.hswebframework.web.authorization.exception;

/**
 * 权限验证异常
 *
 * @author zhouhao
 * @since 3.0
 */
public class AccessDenyException extends RuntimeException {

    private static final long serialVersionUID = -5135300127303801430L;

    public AccessDenyException() {
        this("权限不足,拒绝访问!");
    }

    public AccessDenyException(String message) {
        super(message);
    }

    public AccessDenyException(String message, Throwable cause) {
        super(message, cause);
    }
}
