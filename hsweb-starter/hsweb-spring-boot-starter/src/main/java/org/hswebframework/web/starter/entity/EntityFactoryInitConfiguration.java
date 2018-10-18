package org.hswebframework.web.starter.entity;

import org.hswebframework.web.commons.entity.factory.DefaultPropertyCopier;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.commons.entity.factory.PropertyCopier;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class EntityFactoryInitConfiguration implements BeanPostProcessor {

    @Autowired
    private MapperEntityFactory mapperEntityFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultPropertyCopier) {
            mapperEntityFactory.setDefaultPropertyCopier(((DefaultPropertyCopier) bean));
        } else if (bean instanceof PropertyCopier) {
            mapperEntityFactory.addCopier(((PropertyCopier) bean));
        }
        if (bean instanceof EntityMappingCustomizer) {
            ((EntityMappingCustomizer) bean).customize(mapperEntityFactory);
        }
        return bean;
    }
}