package org.hswebframework.web.async;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class TransactionSupportAsyncJobServiceTest extends SimpleWebApplicationTests {

    @Autowired
    private SqlExecutor sqlExecutor;

    @Autowired
    private AsyncJobService asyncJobService;

    @Configuration
    @EnableConfigurationProperties(DataSourceProperties.class)
    public static class DataSourceConfig {
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource dataSource(DataSourceProperties properties) {
            return properties.initializeDataSourceBuilder().build();
        }
    }

    @Test
    public void test() throws Exception {

        sqlExecutor.exec("create table test(id varchar(32))");

        try {
            BatchAsyncJobContainer jobContainer = asyncJobService.batch();

            for (int i = 0; i < 100; i++) {
                jobContainer.submit(() -> sqlExecutor.insert("insert into test values('test')", null), true);
            }
            jobContainer.submit(() -> {
                Thread.sleep(200);
                throw new RuntimeException();
            }, true);
            System.out.println(jobContainer.getResult().size());
        } catch (Exception ignore) {
        }
        Assert.assertTrue(sqlExecutor.list("select * from test").isEmpty());
    }
}