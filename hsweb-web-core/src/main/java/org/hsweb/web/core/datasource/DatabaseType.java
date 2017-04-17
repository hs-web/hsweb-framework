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

import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.function.Predicate;

public enum DatabaseType {
    unknown(null, null, null, null, String::isEmpty),
    mysql("com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "select 1", Dialect.MYSQL, createUrlPredicate("mysql")),
    h2("org.h2.Driver", "org.h2.jdbcx.JdbcDataSource", "select 1", Dialect.H2, createUrlPredicate("h2")),
    oracle("oracle.jdbc.driver.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource", "select 1 from dual", Dialect.ORACLE, createUrlPredicate("oracle")),
    jtds_sqlserver("net.sourceforge.jtds.jdbc.Driver", "net.sourceforge.jtds.jdbcx.JtdsDataSource", "select 1 t", Dialect.MSSQL, createUrlPredicate("jtds:sqlserver")),
    sqlserver("com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerXADataSource", "select 1 t", Dialect.MSSQL, createUrlPredicate("sqlserver"));

    static Predicate<String> createUrlPredicate(String name) {
        return url -> {
            String urlWithoutPrefix = url.substring("jdbc".length()).toLowerCase();
            String prefix = ":" + name.toLowerCase() + ":";
            return urlWithoutPrefix.startsWith(prefix);
        };
    }

    DatabaseType(String driverClassName, String xaDataSourceClassName, String testQuery, Dialect dialect, Predicate<String> urlPredicate) {
        this.driverClassName = driverClassName;
        this.testQuery = testQuery;
        this.xaDataSourceClassName = xaDataSourceClassName;
        this.dialect = dialect;
        this.urlPredicate = urlPredicate;
    }


    private final String testQuery;

    private final String driverClassName;

    private final String xaDataSourceClassName;

    private final Dialect dialect;

    private final Predicate<String> urlPredicate;

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
        if (StringUtils.hasLength(url)) {
            return Arrays.stream(values()).filter(type -> type.urlPredicate.test(url)).findFirst().orElse(unknown);
        }
        return unknown;
    }
}