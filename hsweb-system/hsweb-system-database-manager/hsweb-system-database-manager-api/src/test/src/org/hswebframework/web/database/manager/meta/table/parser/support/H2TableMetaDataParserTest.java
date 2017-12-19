package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.TableMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2TableMetaDataParserTest {
    H2TableMetaDataParser parser;

    @Before
    public void init() throws Exception {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:/test;", "sa", "");
        SqlExecutor sqlExecutor = new AbstractJdbcSqlExecutor() {
            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public void releaseConnection(Connection connection) throws SQLException {
                //connection.close();
            }
        };
        sqlExecutor.exec("create table test(id varchar(32) not null,name varchar(128) ,age number(32))");
        parser = new H2TableMetaDataParser(sqlExecutor);
    }

    @Test
    public void testParse() throws SQLException {
        TableMetadata metaData = parser.parse("test");

        Assert.assertNotNull(metaData);
        Assert.assertNotNull(metaData.getColumns());

    }
}