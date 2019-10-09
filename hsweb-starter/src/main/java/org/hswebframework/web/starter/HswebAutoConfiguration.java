package org.hswebframework.web.starter;

import org.hswebframework.web.crud.entity.factory.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;
@Configuration
public class HswebAutoConfiguration  {


    @Bean
    @ConditionalOnMissingBean
    public EntityFactory entityFactory(){
        return new MapperEntityFactory();
    }
}
