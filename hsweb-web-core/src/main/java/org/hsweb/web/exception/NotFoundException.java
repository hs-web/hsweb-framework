package org.hsweb.web.exception;

/**
 * Created by zhouhao on 16-4-29.
 */
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(message, 404);
    }
}
