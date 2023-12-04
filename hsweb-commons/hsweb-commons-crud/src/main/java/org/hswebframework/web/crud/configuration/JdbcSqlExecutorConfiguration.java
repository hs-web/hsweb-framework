package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.web.crud.sql.DefaultJdbcExecutor;
import org.hswebframework.web.crud.sql.DefaultJdbcReactiveExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
public class JdbcSqlExecutorConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SyncSqlExecutor syncSqlExecutor() {
        return new DefaultJdbcExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveSqlExecutor reactiveSqlExecutor() {
        return new DefaultJdbcReactiveExecutor();
    }

}