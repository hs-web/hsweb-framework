package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.web.crud.sql.DefaultJdbcExecutor;
import org.hswebframework.web.crud.sql.DefaultJdbcReactiveExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@AutoConfiguration(after = DataSourceAutoConfiguration.class,
before = TransactionAutoConfiguration.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
public class JdbcSqlExecutorConfiguration {

    @Bean
    @Primary
    DataSourceTransactionManager transactionManager(Environment environment, DataSource dataSource,
                                                                     ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        DataSourceTransactionManager transactionManager = createTransactionManager(environment, dataSource);
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
        return transactionManager;
    }

    @Bean
    DataSourceTransactionManager connectionFactoryTransactionManager(Environment environment, DataSource dataSource,
                                                    ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        DataSourceTransactionManager transactionManager = createTransactionManager(environment, dataSource);
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
        return transactionManager;
    }

    private DataSourceTransactionManager createTransactionManager(Environment environment, DataSource dataSource) {
        return environment.getProperty("spring.dao.exceptiontranslation.enabled", Boolean.class, Boolean.TRUE)
            ? new JdbcTransactionManager(dataSource) : new DataSourceTransactionManager(dataSource);
    }


    @Bean
    @ConditionalOnMissingBean
    public SyncSqlExecutor syncSqlExecutor(DataSource dataSource) {
        return new DefaultJdbcExecutor(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveSqlExecutor reactiveSqlExecutor(DataSource dataSource) {
        return new DefaultJdbcReactiveExecutor(dataSource);
    }

}