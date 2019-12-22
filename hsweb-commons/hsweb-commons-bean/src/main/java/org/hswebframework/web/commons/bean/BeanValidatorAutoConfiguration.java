package org.hswebframework.web.commons.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;
import java.util.Objects;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
public class BeanValidatorAutoConfiguration implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        BeanValidator.validator = Objects.requireNonNull(applicationContext.getBean(Validator.class), "The bean of type Validator are required.");
    }
}
