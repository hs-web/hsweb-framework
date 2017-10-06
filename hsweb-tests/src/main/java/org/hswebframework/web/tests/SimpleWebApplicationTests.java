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

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleWebApplicationTests.Config.class)
public class SimpleWebApplicationTests extends AbstractTransactionalJUnit4SpringContextTests {

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

    }
}
