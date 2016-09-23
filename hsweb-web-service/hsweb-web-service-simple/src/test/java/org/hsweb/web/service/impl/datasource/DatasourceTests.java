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

package org.hsweb.web.service.impl.datasource;

import org.hsweb.ezorm.executor.SqlExecutor;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.datasource.DataSource;
import org.hsweb.web.core.Install;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Date;

public class DatasourceTests extends AbstractTestCase {
    @Resource
    private DataSourceService dataSourceService;

    @Resource
    SqlExecutor sqlExecutor;

    @Resource
    private TestService testService;

    @Resource
    private UserService userService;
    @Resource
    Install install;
    @PostConstruct
    public void init() {
        testService.setSqlExecutor(sqlExecutor);
    }

    @Before
    public void setup() throws Exception {
        dataSourceService.delete("test");
        DataSource dataSource = new DataSource();
        dataSource.setId("test");
        dataSource.setName("test");
        dataSource.setDriver("org.h2.Driver");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setCreateDate(new Date());
        dataSource.setUrl("jdbc:h2:file:./data/h2db2;DB_CLOSE_ON_EXIT=FALSE");

        dataSourceService.insert(dataSource);

        DynamicDataSource.use("test");
        install.install();//安装新的数据库

        DynamicDataSource.useDefault();
        userService.select(QueryParam.build());
        DynamicDataSource.use("test");
        userService.select(QueryParam.build());

    }

    @Test
    public void testGetFieldList() throws Exception {
        testService.test();
    }

}
