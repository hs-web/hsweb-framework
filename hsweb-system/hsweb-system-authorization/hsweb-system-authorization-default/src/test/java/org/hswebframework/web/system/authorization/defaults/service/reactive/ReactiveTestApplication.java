package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;

@SpringBootApplication(exclude = {
         //TransactionAutoConfiguration.class,
        JdbcSqlExecutorConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ImportAutoConfiguration({
        R2dbcTransactionManagerAutoConfiguration.class,
        DefaultAuthorizationAutoConfiguration.class
})
public class ReactiveTestApplication {


}
