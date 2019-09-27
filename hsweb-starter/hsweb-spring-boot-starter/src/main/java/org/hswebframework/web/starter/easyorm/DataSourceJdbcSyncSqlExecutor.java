package org.hswebframework.web.starter.easyorm;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.jdbc.JdbcSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public abstract class DataSourceJdbcSyncSqlExecutor extends JdbcSyncSqlExecutor {

    protected abstract DataSource getDataSource();

    @Override
    public Connection getConnection(SqlRequest sqlRequest) {
        DataSource dataSource = getDataSource();

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (log.isDebugEnabled()) {
            log.debug("DataSource ({}) JDBC Connection [{}] will {}be managed by Spring", dataSource, connection, (isConnectionTransactional ? "" : "not "));
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection, SqlRequest sqlRequest) {
        DataSource dataSource = getDataSource();

        if (log.isDebugEnabled()) {
            log.debug("Releasing DataSource ({}) JDBC Connection [{}]", dataSource, connection);
        }
        try {
            DataSourceUtils.doReleaseConnection(connection, dataSource);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            try {
                connection.close();
            } catch (Exception e2) {
                log.error(e2.getMessage(), e2);
            }
        }
    }

    @Override
    @Transactional
    public int update(SqlRequest request) {
        return super.update(request);
    }

    @Override
    @Transactional(readOnly = true)
    public <T, R> R select(SqlRequest request, ResultWrapper<T, R> wrapper) {
        return super.select(request, wrapper);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void execute(SqlRequest request) {
        super.execute(request);
    }
}
