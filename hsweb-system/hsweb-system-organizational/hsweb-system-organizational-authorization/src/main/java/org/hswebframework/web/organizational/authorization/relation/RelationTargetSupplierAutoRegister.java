package org.hswebframework.web.organizational.authorization.relation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author zhouhao
 * @since 3.0
 */
public class RelationTargetSupplierAutoRegister implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RelationTargetSupplier) {
            RelationTargetHolder.addSupplier(((RelationTargetSupplier) bean));
        }
        return bean;
    }
}
