package org.hswebframework.web.commons.bean;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public final class BeanValidator {

    private BeanValidator() {
    }

    static volatile Validator validator;

    public static Validator getValidator() {
        if (validator == null) {
            synchronized (BeanValidator.class) {
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                return validator = factory.getValidator();
            }
        }
        return validator;
    }

    public static <T> T tryValidate(T bean, Class... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validate(bean, group);
        if (!violations.isEmpty()) {
            SimpleValidateResults results = new SimpleValidateResults();
            for (ConstraintViolation<T> violation : violations) {
                results.addResult(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new ValidationException(results);
        }
        return bean;
    }
}
