package org.hswebframework.web.api.crud.entity;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
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
