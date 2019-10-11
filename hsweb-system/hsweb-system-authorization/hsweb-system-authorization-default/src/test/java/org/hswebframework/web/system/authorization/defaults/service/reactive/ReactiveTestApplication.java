package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.hswebframework.web.system.authorization.defaults.service.DefaultReactiveUserService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.reactive.ReactiveTransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {
         //TransactionAutoConfiguration.class,
        JdbcSqlExecutorConfiguration.class
})
@ImportAutoConfiguration(ReactiveTransactionAutoConfiguration.class)
public class ReactiveTestApplication {


    @Bean
    public DefaultReactiveUserService defaultReactiveUserService(){

        return new DefaultReactiveUserService();
    }

}
