package org.hswebframework.web.authorization.exception;

import lombok.Getter;
import org.hswebframework.web.exception.I18nSupportException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

/**
 * 权限验证异常
 *
 * @author zhouhao
 * @since 3.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDenyException extends I18nSupportException {

    private static final long serialVersionUID = -5135300127303801430L;

    @Getter
    private String code;

    public AccessDenyException() {
        this("error.access_denied");
    }

    public AccessDenyException(String message) {
        super(message);
    }

    public AccessDenyException(String permission, Set<String> actions) {
        super("error.permission_denied", permission, actions);
    }

    public AccessDenyException(String message, String code) {
        this(message, code, null);
    }

    public AccessDenyException(String message, Throwable cause) {
        this(message, "access_denied", cause);
    }

    public AccessDenyException(String message, String code, Throwable cause) {
        super(message, cause,code);
        this.code = code;
    }
}
