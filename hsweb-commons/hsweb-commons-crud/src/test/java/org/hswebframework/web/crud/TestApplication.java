package org.hswebframework.web.crud;

import org.hswebframework.web.crud.entity.factory.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class TestApplication {

    @Bean
    public EntityFactory entityFactory(){
        return new MapperEntityFactory();
    }
}
