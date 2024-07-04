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
@Getter
public class AccessDenyException extends I18nSupportException {

    private static final long serialVersionUID = -5135300127303801430L;

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
        super(message, cause, code);
        this.code = code;
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends AccessDenyException {
        public NoStackTrace() {
            super();
        }

        public NoStackTrace(String message) {
            super(message);
        }

        public NoStackTrace(String permission, Set<String> actions) {
            super(permission, actions);
        }

        public NoStackTrace(String message, String code) {
            super(message, code);
        }

        public NoStackTrace(String message, Throwable cause) {
            super(message, cause);
        }

        public NoStackTrace(String message, String code, Throwable cause) {
            super(message, code, cause);
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
