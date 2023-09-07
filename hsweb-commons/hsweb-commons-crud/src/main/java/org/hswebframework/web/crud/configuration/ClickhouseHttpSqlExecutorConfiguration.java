package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.supports.clickhouse.ClickhouseRestfulSqlExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author dengpengyu
 */
@Configuration
@EnableConfigurationProperties(ClickhouseProperties.class)
//@ConditionalOnMissingBean(ConnectionFactory.class)
public class ClickhouseHttpSqlExecutorConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ReactiveSqlExecutor reactiveSqlExecutor(ClickhouseProperties properties) {

        WebClient clickhouseWebClient = WebClient
                .builder()
                .baseUrl(properties.getUrl())
                .defaultHeader("X-ClickHouse-User", properties.getUsername())
                .defaultHeader("X-ClickHouse-Key", properties.getPassword())
                .build();

        return new ClickhouseRestfulSqlExecutor(clickhouseWebClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public SyncSqlExecutor syncSqlExecutor(ReactiveSqlExecutor reactiveSqlExecutor) {
        return ReactiveSyncSqlExecutor.of(reactiveSqlExecutor);
    }
}