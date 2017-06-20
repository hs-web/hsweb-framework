package org.hswebframework.web.datasource.jta;

import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@ConditionalOnClass(SqlExecutor.class)
public class JtaJdbcSqlExecutorAutoConfiguration {
    @Bean
    public JtaJdbcSqlExecutor jtaJdbcSqlExecutor() {
        return new JtaJdbcSqlExecutor();
    }
}
