package org.hswebframework.web.crud;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Configuration
public class TestApplication {

    @Bean
    public EntityFactory entityFactory(){
        return new MapperEntityFactory();
    }
}
