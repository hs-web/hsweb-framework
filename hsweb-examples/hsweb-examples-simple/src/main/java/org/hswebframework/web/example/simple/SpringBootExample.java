/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.example.simple;

import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.dao.datasource.DataSourceHolder;
import org.hswebframework.web.dao.datasource.DatabaseType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@EnableAutoConfiguration
@SpringBootApplication
@Configuration
@RequestMapping
public class SpringBootExample {

    @Bean
    @ConditionalOnMissingBean(SqlExecutor.class)
    public SqlExecutor sqlExecutor(DataSource dataSource) {
        DataSourceHolder.install(dataSource, DatabaseType.h2);
        return new AbstractJdbcSqlExecutor() {
            @Override
            public Connection getConnection() {
                return DataSourceUtils.getConnection(dataSource);
            }

            @Override
            public void releaseConnection(Connection connection) throws SQLException {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class);
    }
}
