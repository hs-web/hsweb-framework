package org.hsweb.web.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Created by zhouhao on 16-4-25.
 */
@Configuration
public class HibernateValidatorAutoConfiguration {

    @Bean
    public Validator getHibernateValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator;
    }
}
