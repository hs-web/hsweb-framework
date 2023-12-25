package org.hswebframework.web.crud;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.entity.factory.EntityMappingCustomizer;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.crud.events.TestEntityListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Configuration
public class TestApplication {

    @Bean
    public EntityFactory entityFactory(ObjectProvider<EntityMappingCustomizer> customizers) {
        MapperEntityFactory factory = new MapperEntityFactory();
        customizers.forEach(customizer -> customizer.custom(factory));
        return factory;
    }

    @Bean
    public TestEntityListener testEntityListener(){
        return new TestEntityListener();
    }
}
