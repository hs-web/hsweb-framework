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

package org.hsweb.web.datasource.dynamic;

import org.hsweb.ezorm.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.executor.SQL;
import org.hsweb.ezorm.meta.expand.ObjectWrapper;
import org.hsweb.ezorm.meta.expand.SimpleMapWrapper;
import org.hsweb.ezorm.render.support.simple.SimpleSQL;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 动态数据源sql执行器
 */
public class DynamicDataSourceSqlExecutorService extends AbstractJdbcSqlExecutor implements ExpressionScopeBean {

    @Resource
    protected DynamicDataSource dynamicDataSource;

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(dynamicDataSource.getActiveDataSource());
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        DataSourceUtils.releaseConnection(connection, dynamicDataSource.getActiveDataSource());
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> list(SQL sql, ObjectWrapper<T> wrapper) throws SQLException {
        return super.list(sql, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T single(SQL sql, ObjectWrapper<T> wrapper) throws SQLException {
        return super.single(sql, wrapper);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(SQL sql) throws SQLException {
        List<Map<String, Object>> data = list(sql, new SimpleMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(SQL sql) throws Exception {
        Map<String, Object> data = single(sql, new SimpleMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql) throws Exception {
        List<Map<String, Object>> data = list(create(sql), new SimpleMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql, Map<String, Object> param) throws Exception {
        List<Map<String, Object>> data = list(create(sql, param), new SimpleMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(String sql) throws Exception {
        Map<String, Object> data = single(create(sql));
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(String sql, Map<String, Object> param) throws Exception {
        Map<String, Object> data = single(create(sql, param));
        return data;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void exec(SQL sql) throws SQLException {
        super.exec(sql);
    }

    public SQL create(String sql) {
        return new SimpleSQL(sql);
    }

    public SQL create(String sql, Map<String, Object> param) {
        SimpleSQL sql1 = new SimpleSQL(sql, param);
        return sql1;
    }

}
