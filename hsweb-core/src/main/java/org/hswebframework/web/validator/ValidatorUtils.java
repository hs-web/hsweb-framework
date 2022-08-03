package org.hswebframework.web.validator;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.i18n.ContextLocaleResolver;

import javax.validation.*;
import java.util.Set;

public final class ValidatorUtils {

    private ValidatorUtils() {
    }

    static volatile Validator validator;

    public static Validator getValidator() {
        if (validator == null) {
            synchronized (ValidatorUtils.class) {
                if (validator != null) {
                    return validator;
                }
                Configuration<?> configuration = Validation
                        .byDefaultProvider()
                        .configure();
                configuration.addProperty(BaseHibernateValidatorConfiguration.LOCALE_RESOLVER_CLASSNAME,
                                          ContextLocaleResolver.class.getName());
                configuration.messageInterpolator(configuration.getDefaultMessageInterpolator());

                ValidatorFactory factory = configuration.buildValidatorFactory();

                return validator = factory.getValidator();
            }
        }
        return validator;
    }

    public static <T> T tryValidate(T bean, Class<?>... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validate(bean, group);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations).withSource(bean);
        }

        return bean;
    }

    public static <T> T tryValidate(T bean, String property, Class<?>... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validateProperty(bean, property, group);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations).withSource(bean);
        }

        return bean;
    }

    public static <T> void tryValidate(Class<T> bean, String property, Object value, Class<?>... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validateValue(bean, property, value, group);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations).withSource(value);
        }
    }

}
