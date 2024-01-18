package org.hswebframework.web.dictionary;

import org.hswebframework.web.crud.configuration.JdbcSqlExecutorConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;

@SpringBootApplication(exclude = {
        JdbcSqlExecutorConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ImportAutoConfiguration({
        R2dbcTransactionManagerAutoConfiguration.class
})
public class ReactiveTestApplication {


}
