package org.hswebframework.web.service;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface Validator<T> {
    boolean validate(T data);

    default String getErrorMessage() {
        return "{validation_fail}";
    }
}
