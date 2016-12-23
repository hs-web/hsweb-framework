/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
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

package org.hswebframework.web.dao.datasource;

import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hswebframwork.utils.StringUtils;

public enum DatabaseType {
    unknown(null, null, null, null),
    mysql("com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "select 1", Dialect.MYSQL),
    h2("org.h2.Driver", "org.h2.jdbcx.JdbcDataSource", "select 1", Dialect.H2),
    oracle("oracle.jdbc.driver.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource", "select 1 from dual", Dialect.ORACLE);

    DatabaseType(String driverClassName, String xaDataSourceClassName, String testQuery, Dialect dialect) {
        this.driverClassName = driverClassName;
        this.testQuery = testQuery;
        this.xaDataSourceClassName = xaDataSourceClassName;
        this.dialect = dialect;
    }

    private final String testQuery;

    private final String driverClassName;

    private final String xaDataSourceClassName;

    private final Dialect dialect;

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getXaDataSourceClassName() {
        return xaDataSourceClassName;
    }

    public String getTestQuery() {
        return testQuery;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public static DatabaseType fromJdbcUrl(String url) {
        if (!StringUtils.isNullOrEmpty(url)) {
            if (!url.trim().startsWith("jdbc")) {
                throw new UnsupportedOperationException("URL must start with 'jdbc'");
            }
            String urlWithoutPrefix = url.substring("jdbc".length()).toLowerCase();
            for (DatabaseType driver : values()) {
                String prefix = ":" + driver.name().toLowerCase() + ":";
                if (driver != unknown && urlWithoutPrefix.startsWith(prefix)) {
                    return driver;
                }
            }
        }
        return unknown;
    }
}