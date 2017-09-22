package org.hswebframework.web.datasource.jta;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @see JtaJdbcSqlExecutor
 * @since 3.0
 */
@ConditionalOnClass(SqlExecutor.class)
@Configuration
public class JtaJdbcSqlExecutorAutoConfiguration {
    @Bean
    public JtaJdbcSqlExecutor jtaJdbcSqlExecutor() {
        return new JtaJdbcSqlExecutor();
    }
}
