package org.hswebframework.web.datasource;

import org.hswebframework.ezorm.core.ObjectWrapper;
import org.hswebframework.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SQL;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author zhouhao
 */
@Transactional(rollbackFor = Throwable.class)
public class DefaultJdbcExecutor extends AbstractJdbcSqlExecutor {

    protected String getDatasourceId() {
        String id = DataSourceHolder.switcher().currentDataSourceId();
        return id == null ? "default" : id;
    }

    @Override
    public Connection getConnection() {
        DataSource dataSource = DataSourceHolder.currentDataSource().getNative();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (logger.isDebugEnabled()) {
            logger.debug("DataSource ({}) JDBC Connection [{}] will {}be managed by Spring", getDatasourceId(), connection, (isConnectionTransactional ? "" : "not "));
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing DataSource ({}) JDBC Connection [{}]", getDatasourceId(), connection);
        }
        try {
            DataSourceUtils.doReleaseConnection(connection, DataSourceHolder.currentDataSource().getNative());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            try {
                connection.close();
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> list(SQL sql, ObjectWrapper<T> wrapper) throws SQLException {
        return super.list(sql, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public void list(String sql, Object params, Consumer<Map<String, Object>> consumer) throws SQLException {
        super.list(sql, params, consumer);
    }

    @Override
    @Transactional(readOnly = true)
    public void list(String sql, Consumer<Map<String, Object>> consumer) throws SQLException {
        super.list(sql, consumer);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> list(String sql, Object params, ObjectWrapper<T> wrapper) throws SQLException {
        return super.list(sql, params, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> List<T> list(String sql, ObjectWrapper<T> wrapper) throws SQLException {
        return super.list(sql, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql) throws SQLException {
        return super.list(sql);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(SQL sql) throws SQLException {
        return super.list(sql);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String sql, Object params) throws SQLException {
        return super.list(sql, params);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void exec(SQL sql) throws SQLException {
        super.exec(sql);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void exec(String sql) throws SQLException {
        super.exec(sql);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void exec(String sql, Object params) throws SQLException {
        super.exec(sql, params);
    }
}
