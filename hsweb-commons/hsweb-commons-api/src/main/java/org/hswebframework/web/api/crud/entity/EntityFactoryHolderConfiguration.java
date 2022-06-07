package org.hswebframework.web.api.crud.entity;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EntityFactoryHolderConfiguration {


    @Bean
    public ApplicationContextAware entityFactoryHolder() {
        return context -> {
            try {
                EntityFactoryHolder.FACTORY = context.getBean(EntityFactory.class);
            } catch (BeansException ignore) {
                
            }
        };
    }

}
