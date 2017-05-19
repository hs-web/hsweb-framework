package org.hswebframework.web.datasource.jta;

import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hsweb.ezorm.rdb.meta.parser.TableMetaParser;
import org.hsweb.ezorm.rdb.render.SqlRender;
import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.simple.SimpleDatabase;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.expands.script.engine.SpEL.SpElEngine;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.datasource.annotation.UseDataSource;
import org.hswebframework.web.datasource.annotation.UseDefaultDataSource;
import org.hswebframework.web.datasource.starter.AopDataSourceSwitcherAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.expression.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpSessionScope;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootTest(properties = "application.yml", classes = SimpleAtomikosTests.Config.class)
@RunWith(SpringRunner.class)
public class SimpleAtomikosTests {

    @Configuration
    @SpringBootApplication
    @EnableJms
    @ImportAutoConfiguration(AopDataSourceSwitcherAutoConfiguration.class)
    @EnableAspectJAutoProxy
    public static class Config {
        @Bean
        public SqlExecutor sqlExecutor() {
            return new AbstractJdbcSqlExecutor() {

                @Override
                @Transactional(propagation = Propagation.NOT_SUPPORTED)
                public void exec(SQL sql) throws SQLException {
                    super.exec(sql);
                }

                @Override
                public Connection getConnection() {
                    return DataSourceUtils.getConnection(DataSourceHolder.currentDataSource());
                }

                @Override
                public void releaseConnection(Connection connection) throws SQLException {
                    DataSourceUtils.releaseConnection(connection, DataSourceHolder.currentDataSource());
                }
            };
        }

        @Bean
        public DynDsTest transTest(SqlExecutor sqlExecutor) {
            SimpleDatabase database = new SimpleDatabase(new DynDatabaseMeta(sqlExecutor), sqlExecutor);
            database.setAutoParse(true);
            return new DynDsTest(database);
        }

        public class DynDatabaseMeta extends RDBDatabaseMetaData {
            private Map<DatabaseType, Dialect>             dialectMap;
            private Map<DatabaseType, RDBDatabaseMetaData> metaDataMap;
            private Map<DatabaseType, TableMetaParser>     parserMap;

            public DynDatabaseMeta(SqlExecutor sqlExecutor) {
                dialectMap = new HashMap<>();
                metaDataMap = new HashMap<>();
                parserMap = new HashMap<>();
                dialectMap.put(DatabaseType.h2, Dialect.H2);
                dialectMap.put(DatabaseType.mysql, Dialect.MYSQL);
                dialectMap.put(DatabaseType.oracle, Dialect.ORACLE);
                metaDataMap.put(DatabaseType.h2, new H2RDBDatabaseMetaData());
                metaDataMap.put(DatabaseType.mysql, new MysqlRDBDatabaseMetaData());
                metaDataMap.put(DatabaseType.oracle, new OracleRDBDatabaseMetaData());

                parserMap.put(DatabaseType.h2, new H2TableMetaParser(sqlExecutor));
                parserMap.put(DatabaseType.mysql, new MysqlTableMetaParser(sqlExecutor));
                parserMap.put(DatabaseType.oracle, new OracleTableMetaParser(sqlExecutor));
            }

            @Override
            public RDBTableMetaData putTable(RDBTableMetaData tableMetaData) {
                return metaDataMap.get(DataSourceHolder.currentDatabaseType()).putTable(tableMetaData);
            }

            @Override
            public TableMetaParser getParser() {
                return parserMap.get(DataSourceHolder.currentDatabaseType());
            }

            @Override
            public Dialect getDialect() {
                return dialectMap.get(DataSourceHolder.currentDatabaseType());
            }

            @Override
            public void init() {
                metaDataMap.values().forEach(RDBDatabaseMetaData::init);
            }

            @Override
            public SqlRender getRenderer(SqlRender.TYPE type) {
                return metaDataMap.get(DataSourceHolder.currentDatabaseType()).getRenderer(type);
            }

            @Override
            public String getName() {
                return metaDataMap.get(DataSourceHolder.currentDatabaseType()).getName();
            }
        }
    }

    @Autowired
    private DynDsTest dynDsTest;

    @Test
    @Transactional
    public void testForEach() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            test();
        }
    }

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    @Rollback(false)
    public void test() {
        try {
            DataSourceHolder.switcher().reset();

            dynDsTest.testCreateTable();
            dynDsTest.testInsert();
            DataSourceHolder.switcher().use("test_ds");

            dynDsTest.testCreateTable();

            DataSourceHolder.switcher().use("test_ds2");

            dynDsTest.testCreateTable();
            System.out.println(DataSourceHolder.switcher().currentDataSourceId());
            System.out.println(dynDsTest.testQuery());

            DataSourceHolder.switcher().useLast();
            System.out.println(DataSourceHolder.switcher().currentDataSourceId());
            System.out.println(dynDsTest.testQuery());

            DataSourceHolder.switcher().useLast();
            System.out.println(DataSourceHolder.switcher().currentDataSourceId());
            System.out.println(dynDsTest.testQuery());
            jmsTemplate.convertAndSend("test", "hello");
            Thread.sleep(1000);
            //   throw new RuntimeException();
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Transactional
    public static class DynDsTest {
        private RDBDatabase database;

        public void testCreateTable() throws SQLException {
            database.createOrAlter("s_user")
                    .addColumn().name("name").varchar(32).commit()
                    .commit();
        }

        public DynDsTest(RDBDatabase database) {
            this.database = database;
        }

        public void testInsert() throws SQLException {
            database.getTable("s_user").createInsert()
                    .value(Collections.singletonMap("name", "test"))
                    .exec();
        }


        public List testQuery() throws SQLException {
            return database.getTable("s_user").createQuery().list();
        }
    }

}