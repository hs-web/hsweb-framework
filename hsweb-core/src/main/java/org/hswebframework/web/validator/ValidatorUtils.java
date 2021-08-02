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

    @SuppressWarnings("all")
    public static <T> T tryValidate(T bean, Class... group) {
        Set<ConstraintViolation<T>> violations = getValidator().validate(bean, group);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }

        return bean;
    }

}
