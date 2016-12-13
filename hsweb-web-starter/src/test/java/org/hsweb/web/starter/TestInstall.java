package org.hsweb.web.starter;

import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hsweb.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hsweb.ezorm.rdb.simple.SimpleDatabase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author zhouhao
 * @TODO
 */
public class TestInstall {

    SqlExecutor sqlExecutor;
    RDBDatabase database;
    Connection  connection;

    @Before
    public void setup() throws Exception {
//        Class.forName("org.h2.Driver");
//        Connection connection = DriverManager.getConnection("jdbc:h2:file:./target/data/h2db;", "sa", "");

        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:file:./target/data/h2db;", "sa", "");
//        Class.forName("com.mysql.jdbc.Driver");
//        connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test_db?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "19920622");

        sqlExecutor = new AbstractJdbcSqlExecutor() {

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public void releaseConnection(Connection connection) throws SQLException {
                //connection.close();
            }
        };
        RDBDatabaseMetaData databaseMetaData = new H2RDBDatabaseMetaData();
        database = new SimpleDatabase(databaseMetaData, sqlExecutor);
    }

    @Test
    public void testInstall() throws Exception {
        SystemVersion version = new SystemVersion();
        version.setVersion(2, 2, 4, true);
        version.setName("hsweb");
        version.setComment("测试");
        SystemInitialize initialize = new SystemInitialize(version, sqlExecutor, database, "sa", "h2");
        initialize.afterPropertiesSet();
        Assert.assertEquals(sqlExecutor.tableExists("s_oauth2_client"), true);
    }

    @After
    public void uninstall() throws SQLException {
        connection.close();
    }

}
