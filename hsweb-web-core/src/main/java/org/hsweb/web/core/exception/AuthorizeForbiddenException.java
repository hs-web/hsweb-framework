package org.hsweb.web.core.exception;

/**
 * Created by 浩 on 2015-12-23 0023.
 */
public class AuthorizeForbiddenException extends BusinessException {
    private static final long serialVersionUID = 2422918455013900645L;

    public AuthorizeForbiddenException(String message) {
        this(message, 403);
    }

    public AuthorizeForbiddenException(String message, int status) {
        super(message, status);
    }

    public AuthorizeForbiddenException(String message, Throwable cause, int status) {
        super(message, cause, status);
    }
}
