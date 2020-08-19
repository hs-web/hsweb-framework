package org.hswebframework.web.authorization.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 权限验证异常
 *
 * @author zhouhao
 * @since 3.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
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
        this(message,"access_denied", cause);
    }

    public AccessDenyException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
