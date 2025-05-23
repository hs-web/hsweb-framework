package org.hswebframework.web.crud.configuration;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.EntityMappingCustomizer;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class EntityFactoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EntityFactory entityFactory(ObjectProvider<EntityMappingCustomizer> customizers) {
        MapperEntityFactory factory = new MapperEntityFactory();
        for (EntityMappingCustomizer customizer : customizers) {
            customizer.custom(factory);
        }
        return factory;
    }

}
