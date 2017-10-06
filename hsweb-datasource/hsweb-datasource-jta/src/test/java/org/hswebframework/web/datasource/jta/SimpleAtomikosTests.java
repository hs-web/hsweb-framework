package org.hswebframework.web.datasource.jta;

import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.TableMetaParser;
import org.hswebframework.ezorm.rdb.render.SqlRender;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.OracleRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.simple.SimpleDatabase;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @EnableAspectJAutoProxy
    public static class Config {

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

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    @Rollback(false)
    public void test() throws SQLException, InterruptedException {
        DataSourceHolder.switcher().reset();

        dynDsTest.testCreateTable();
        dynDsTest.testInsert();
        DataSourceHolder.switcher().use("test_ds");

        dynDsTest.testCreateTable();

        DataSourceHolder.switcher().use("test_ds2");

        dynDsTest.testCreateTable();
        Assert.assertEquals(DataSourceHolder.switcher().currentDataSourceId(), "test_ds2");
        Assert.assertTrue(dynDsTest.testQuery().isEmpty());

        DataSourceHolder.switcher().useLast();
        Assert.assertEquals(DataSourceHolder.switcher().currentDataSourceId(), "test_ds");
        Assert.assertTrue(dynDsTest.testQuery().isEmpty());

        DataSourceHolder.switcher().useLast();
        Assert.assertNull(DataSourceHolder.switcher().currentDataSourceId());
        Assert.assertTrue(dynDsTest.testQuery().size() > 0);

        jmsTemplate.convertAndSend("test", "hello");
        Thread.sleep(1000);
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