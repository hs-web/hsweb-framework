package org.hswebframework.web.starter;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HswebAutoConfiguration  {


    @Bean
    @ConditionalOnMissingBean
    public EntityFactory entityFactory(){
        return new MapperEntityFactory();
    }
}
