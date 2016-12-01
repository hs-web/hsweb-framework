package org.hsweb.web.service.impl.basic;

import org.hsweb.ezorm.core.ObjectWrapper;
import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service(value = "sqlExecutor")
@Transactional(rollbackFor = Throwable.class)
public class SqlExecutorService extends AbstractJdbcSqlExecutor implements ExpressionScopeBean {

    @Resource
    private DataSource dataSource;

    @Override
    public Connection getConnection() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (logger.isDebugEnabled()) {
            logger.debug("JDBC Connection [{}] will {} be managed by Spring", connection, (isConnectionTransactional ? "" : "not"));
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing JDBC Connection [{}]", connection);
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
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
        return super.list(sql);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(SQL sql) throws SQLException {
        return super.single(sql);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql) throws SQLException {
        return super.list(sql);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql, Object param) throws SQLException {
        return super.list(sql, param);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(String sql) throws SQLException {
        return super.single(sql);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> single(String sql, Object param) throws SQLException {
        return super.single(sql, param);
    }

}
