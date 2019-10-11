package org.hswebframework.web.crud.configuration;

import io.r2dbc.spi.ConnectionFactory;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSyncSqlExecutor;
import org.hswebframework.web.crud.sql.DefaultR2dbcExecutor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryAutoConfiguration")
@ConditionalOnBean(ConnectionFactory.class)
public class R2dbcSqlExecutorConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ReactiveSqlExecutor reactiveSqlExecutor() {
        return new DefaultR2dbcExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SyncSqlExecutor syncSqlExecutor(ReactiveSqlExecutor reactiveSqlExecutor) {
        return ReactiveSyncSqlExecutor.of(reactiveSqlExecutor);
    }
}