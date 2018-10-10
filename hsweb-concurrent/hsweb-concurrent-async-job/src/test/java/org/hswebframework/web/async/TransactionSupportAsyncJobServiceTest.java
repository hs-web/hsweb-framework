package org.hswebframework.web.async;

import lombok.SneakyThrows;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
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

    @Before
    @SneakyThrows
    public void init() {
        sqlExecutor.exec("create table test(id varchar(32))");
    }

    @After
    @SneakyThrows
    public void cleanup() {
        sqlExecutor.exec("drop table test");
    }

    @Test
    public void test() throws Exception {
        try {
            BatchAsyncJobContainer jobContainer = asyncJobService.batch();
            jobContainer.submit(() -> {
                Thread.sleep(50);
                throw new RuntimeException("1234");
            }, true);
            for (int i = 0; i < 100; i++) {
                jobContainer.submit(() -> sqlExecutor.insert("insert into test values('test')", null), true);
            }

            System.out.println(jobContainer.getResult().size());
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        Assert.assertTrue(sqlExecutor.list("select * from test").isEmpty());
    }

    @Test
    public void testSimple() throws Exception {
        try {
            BatchAsyncJobContainer jobContainer = asyncJobService.batch();
            jobContainer.submit(() -> {
                Thread.sleep(10);
                jobContainer.cancel();
                throw new RuntimeException();
            }, false);
            for (int i = 0; i < 100; i++) {
                jobContainer.submit(() -> sqlExecutor.insert("insert into test values('test')", null), false);
            }

            System.out.println(jobContainer.getResult().size());

        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        Assert.assertTrue(sqlExecutor.list("select * from test").size() > 0);
    }
}