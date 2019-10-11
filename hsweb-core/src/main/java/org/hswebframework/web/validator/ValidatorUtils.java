package org.hswebframework.web.validator;

import org.hswebframework.web.exception.ValidationException;

import javax.el.ExpressionFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public final class ValidatorUtils {

    private ValidatorUtils() {
    }

    static volatile Validator validator;

    public static Validator getValidator() {
        if (validator == null) {
            synchronized (ValidatorUtils.class) {
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                return validator = factory.getValidator();
            }
        }
        return validator;
    }

    public static <T> T tryValidate(T bean, Class... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validate(bean, group);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage(), violations);
        }

        return bean;
    }

}
