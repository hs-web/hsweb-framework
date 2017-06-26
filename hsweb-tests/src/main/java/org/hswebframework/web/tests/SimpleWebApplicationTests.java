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

package org.hswebframework.web.tests;

import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.dao.datasource.DataSourceHolder;
import org.hswebframework.web.dao.datasource.DatabaseType;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleWebApplicationTests.Config.class)
public class SimpleWebApplicationTests {

    protected MockMvc               mvc;
    @Resource
    protected WebApplicationContext wac;

    protected MockHttpSession session;

    @Autowired
    protected SqlExecutor sqlExecutor;

    @Autowired
    protected EntityFactory entityFactory;

    @Before
    public void setup() throws Exception {
        this.mvc = webAppContextSetup(this.wac).build();
        this.session = new MockHttpSession();
    }

    private TestProcess test(MockHttpServletRequestBuilder builder) {
        return new TestProcess() {
            @Override
            public TestProcess setUp(TestProcessSetUp testProcessSetUp) {
                testProcessSetUp.setUp(builder);
                return this;
            }

            @Override
            public TestResult exec() throws Exception {
                return () -> mvc.perform(builder);
            }
        };
    }

    protected MockHttpServletRequestBuilder initDefaultSetting(MockHttpServletRequestBuilder builder) {
        return builder.session(session).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON);
    }

    protected TestProcess testGet(String api) throws Exception {
        MockHttpServletRequestBuilder msrb = initDefaultSetting(get(api));
        return test(msrb);
    }

    protected TestProcess testPost(String api) throws Exception {
        MockHttpServletRequestBuilder msrb = initDefaultSetting(post(api));
        return test(msrb);
    }

    protected TestProcess testDelete(String api) throws Exception {
        MockHttpServletRequestBuilder msrb = initDefaultSetting(delete(api));
        return test(msrb);
    }


    protected TestProcess testPut(String api) throws Exception {
        MockHttpServletRequestBuilder msrb = initDefaultSetting(put(api));
        return test(msrb);
    }

    @Configuration
    @SpringBootApplication
    @WebAppConfiguration
    public static class Config {

        @Bean
        public SqlExecutor sqlExecutor(DataSource dataSource) throws SQLException {
            Connection connection = dataSource.getConnection();
            try {
                DataSourceHolder.install(dataSource, DatabaseType.fromJdbcUrl(connection.getMetaData().getURL()));
            } finally {
                connection.close();
            }
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
    }
}
