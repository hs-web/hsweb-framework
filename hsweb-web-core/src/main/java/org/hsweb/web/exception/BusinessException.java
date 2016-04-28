package org.hsweb.web.exception;

/**
 * 业务异常，用于抛出给前端提示错误信息
 * Created by 浩 on 2015-08-01 0001.
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -695542791928644578L;
    private int status = 400;

    public BusinessException(String message) {
        this(message, 400);
    }

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
