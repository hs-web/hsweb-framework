package org.hswebframework.web.oauth2;

import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

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
