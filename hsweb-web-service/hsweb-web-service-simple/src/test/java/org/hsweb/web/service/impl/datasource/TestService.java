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
import org.hsweb.ezorm.meta.expand.SimpleMapWrapper;
import org.hsweb.ezorm.render.support.simple.SimpleSQL;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
public class TestService {

    private SqlExecutor sqlExecutor;

    public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @Transactional
    public void test() throws SQLException {
        sqlExecutor.exec(new SimpleSQL("drop table if exists s_test"));
        sqlExecutor.exec(new SimpleSQL("create table s_test(name varchar(32))"));
        System.out.println(sqlExecutor.list(new SimpleSQL("select * from s_test"), new SimpleMapWrapper()));
        System.out.println(sqlExecutor.insert(new SimpleSQL("insert into s_test values ('默认数据源')")));
        DynamicDataSource.use("test");
        sqlExecutor.exec(new SimpleSQL("drop table if exists s_test"));
        sqlExecutor.exec(new SimpleSQL("create table s_test(name varchar(32))"));
        System.out.println(sqlExecutor.list(new SimpleSQL("select * from s_test"), new SimpleMapWrapper()));
        System.out.println(sqlExecutor.insert(new SimpleSQL("insert into s_test values ('测试1')")));
        DynamicDataSource.useDefault();
    }
}
