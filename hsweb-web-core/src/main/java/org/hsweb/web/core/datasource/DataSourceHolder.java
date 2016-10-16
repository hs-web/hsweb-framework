/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.core.datasource;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DataSourceHolder {

    private static DynamicDataSource dynamicDataSource;

    private static DataSource defaultDataSource;

    private static DatabaseType defaultDatabaseType;

    @Autowired(required = false)
    private DataSource dataSource;

    @PostConstruct
    public void init() throws SQLException {
        if (null != dataSource) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                install(dataSource, DatabaseType.fromJdbcUrl(connection.getMetaData().getURL()));
                dataSource = null;
            } finally {
                if (null != connection)
                    connection.close();
            }
        }
    }

    public static DataSource getActiveSource() {
        if (dynamicDataSource != null) {
            return dynamicDataSource.getActiveDataSource();
        }
        return defaultDataSource;
    }

    public static DatabaseType getActiveDatabaseType() {
        if (dynamicDataSource != null) {
            return dynamicDataSource.getActiveDataBaseType();
        }
        return defaultDatabaseType;
    }

    public static DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public static DatabaseType getDefaultDatabaseType() {
        return defaultDatabaseType;
    }

    public static void install(DynamicDataSource dynamicDataSource) {
        if (DataSourceHolder.dynamicDataSource != null) {
            throw new UnsupportedOperationException();
        }
        DataSourceHolder.dynamicDataSource = dynamicDataSource;
    }

    public static void install(DataSource dataSource, DatabaseType databaseType) {
        if (DataSourceHolder.defaultDataSource != null) {
            throw new UnsupportedOperationException();
        }
        DataSourceHolder.defaultDataSource = dataSource;
        DataSourceHolder.defaultDatabaseType = databaseType;
    }
}
