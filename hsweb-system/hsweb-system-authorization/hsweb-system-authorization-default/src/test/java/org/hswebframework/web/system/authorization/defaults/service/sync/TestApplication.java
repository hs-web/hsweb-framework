package org.hswebframework.web.system.authorization.defaults.service.sync;

import org.hswebframework.web.crud.configuration.R2dbcSqlExecutorConfiguration;
import org.hswebframework.web.system.authorization.defaults.service.DefaultUserService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        R2dbcSqlExecutorConfiguration.class
})
public class TestApplication {


    @Bean
    public DefaultUserService defaultUserService(){

        return new DefaultUserService();
    }

}
