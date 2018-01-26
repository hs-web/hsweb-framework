package org.hswebframework.web.datasource;

import org.hswebframework.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SQL;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

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
