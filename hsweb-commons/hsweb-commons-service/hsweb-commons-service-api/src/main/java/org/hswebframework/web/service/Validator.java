package org.hswebframework.web.service;

/**
 * @author zhouhao
 */
public interface Validator<T> {
    boolean validate(T data);

    default String getErrorMessage() {
        return "{validation_fail}";
    }
}
