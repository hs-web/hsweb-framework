package org.hsweb.web.service.impl.basic;

import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webbuilder.sql.SQL;
import org.webbuilder.sql.support.common.CommonSql;
import org.webbuilder.sql.support.executor.AbstractJdbcSqlExecutor;
import org.webbuilder.sql.support.executor.HashMapWrapper;
import org.webbuilder.sql.support.executor.ObjectWrapper;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * SQL执行服务类，用于执行原生sql
 * Created by 浩 on 2015-10-09 0009.
 */
@Service(value = "sqlExecutor")
@Transactional(rollbackFor = Throwable.class)
public class SqlExecutorService extends AbstractJdbcSqlExecutor implements ExpressionScopeBean {

    @Resource
    private DataSource dataSource;

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> list(SQL sql, ObjectWrapper<T> wrapper) throws Exception {
        return super.list(sql, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T single(SQL sql, ObjectWrapper<T> wrapper) throws Exception {
        return super.single(sql, wrapper);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(SQL sql) throws Exception {
        List<Map<String, Object>> data = list(sql, new HashMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(SQL sql) throws Exception {
        Map<String, Object> data = single(sql, new HashMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql) throws Exception {
        List<Map<String, Object>> data = list(create(sql), new HashMapWrapper());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql, Map<String, Object> param) throws Exception {
        List<Map<String, Object>> data = list(create(sql, param), new HashMapWrapper());
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

    public SQL create(String sql) {
        return new CommonSql(sql);
    }

    public SQL create(String sql, Map<String, Object> param) {
        CommonSql sql1 = new CommonSql(sql, param);
        return sql1;
    }
}
