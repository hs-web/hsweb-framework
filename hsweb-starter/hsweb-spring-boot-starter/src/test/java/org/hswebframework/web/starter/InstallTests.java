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

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.jdbc.JdbcSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.hswebframework.ezorm.rdb.supports.h2.H2SchemaMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author zhouhao
 */
public class InstallTests {
    SyncSqlExecutor sqlExecutor;
    DatabaseOperator database;
    Connection  connection;

    @Before
    public void setup() throws Exception {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:file:./target/data/h2db;", "sa", "");
        sqlExecutor =new JdbcSyncSqlExecutor() {
            @Override
            public Connection getConnection(SqlRequest sqlRequest) {
                return connection;
            }

            @Override
            public void releaseConnection(Connection connection, SqlRequest sqlRequest) {

            }
        };
        RDBDatabaseMetadata databaseMetaData=new RDBDatabaseMetadata(Dialect.H2);
        databaseMetaData.addFeature(sqlExecutor);
        H2SchemaMetadata schema = new H2SchemaMetadata("PUBLIC");
        databaseMetaData.addSchema(schema);
        databaseMetaData.setCurrentSchema(schema);
        database = DefaultDatabaseOperator.of(databaseMetaData);
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
