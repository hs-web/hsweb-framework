package org.hswebframework.web.system.authorization.defaults.service.reactive;

import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.reactive.ReactiveTransactionAutoConfiguration;

@SpringBootApplication(exclude = {
         //TransactionAutoConfiguration.class,
        JdbcSqlExecutorConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ImportAutoConfiguration({
        ReactiveTransactionAutoConfiguration.class,
        DefaultAuthorizationAutoConfiguration.class
})
public class ReactiveTestApplication {


}
