package org.hsweb.web.exception;

/**
 * Created by 浩 on 2015-12-23 0023.
 */
public class AuthorizeException extends BusinessException {
    private static final long serialVersionUID = 2422918455013900645L;

    public AuthorizeException(String message) {
        super(message);
    }

    public AuthorizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
