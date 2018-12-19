package org.hswebframework.web.authorization.exception;

import lombok.Getter;

/**
 * 权限验证异常
 *
 * @author zhouhao
 * @since 3.0
 */
public class AccessDenyException extends RuntimeException {

    private static final long serialVersionUID = -5135300127303801430L;

    @Getter
    private String code;

    public AccessDenyException() {
        this("权限不足,拒绝访问!");
    }

    public AccessDenyException(String message) {
        super(message);
    }

    public AccessDenyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessDenyException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
