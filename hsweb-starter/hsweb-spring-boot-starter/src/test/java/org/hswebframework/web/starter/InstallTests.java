/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.starter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.H2RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.render.dialect.MysqlRDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.simple.SimpleDatabase;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.starter.init.simple.SimpleDependencyInstaller;
import org.hswebframework.utils.file.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class InstallTests {
    SqlExecutor sqlExecutor;
    RDBDatabase database;
    Connection  connection;

    @Before
    public void setup() throws Exception {
//        Class.forName("com.mysql.jdbc.Driver");
//        connection = DriverManager.getConnection(
//                "jdbc:mysql://localhost/test_db1?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false",
//                "root", "root");
//

        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:file:./target/data/h2db;", "sa", "");
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
//        RDBDatabaseMetaData databaseMetaData = new MysqlRDBDatabaseMetaData("MyISAM");
        database = new SimpleDatabase(databaseMetaData, sqlExecutor);
    }

    @Test
    public void testVersion() {
        SystemVersion version = new SystemVersion();
        version.setVersion("3.0.0");

        SystemVersion version2 = new SystemVersion();
        version2.setVersion("3.0.1");

        SystemVersion version4 = new SystemVersion();
        version4.setVersion("3.0.2");

        Assert.assertEquals(version.compareTo(version2), -1);

        Assert.assertEquals(version.compareTo(version4), -1);
    }

    @Test
    public void testInstall() throws Exception {

        SystemVersion version = new SystemVersion();
        version.setName("test");
        version.setVersion("3.0.0");
        org.hswebframework.web.starter.init.SystemInitialize systemInitialize
                = new org.hswebframework.web.starter.init.SystemInitialize(sqlExecutor, database, version);
        systemInitialize.setExcludeTables(Collections.singletonList("s_user_test"));

        systemInitialize.init();
        systemInitialize.install();

        //  List systems = database.getTable("s_system").createQuery().list();
        //System.out.println(JSON.toJSONString(systems, SerializerFeature.PrettyFormat));
    }

}
