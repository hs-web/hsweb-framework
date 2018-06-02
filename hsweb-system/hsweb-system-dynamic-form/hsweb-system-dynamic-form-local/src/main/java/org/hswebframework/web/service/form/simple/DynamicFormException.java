package org.hswebframework.web.service.form.simple;

/**
 * 动态表单操作异常
 *
 * @author zhouhao
 * @since 3.0
 */
public class DynamicFormException extends RuntimeException {
    public DynamicFormException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicFormException(Throwable cause) {
        super(cause);
    }
}
