package org.hswebframework.web.crud.sql;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.jdbc.JdbcSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author zhouhao
 */

@Slf4j
public class DefaultJdbcExecutor extends JdbcSyncSqlExecutor {

    @Autowired
    private DataSource dataSource;

    public DefaultJdbcExecutor() {
    }

    public DefaultJdbcExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected String getDatasourceId() {
        return DataSourceHolder.switcher().datasource().current().orElse("default");
    }

    @Override
    public Connection getConnection(SqlRequest sqlRequest) {

        DataSource dataSource = DataSourceHolder.isDynamicDataSourceReady() ?
            DataSourceHolder.currentDataSource().getNative() :
            this.dataSource;
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (log.isDebugEnabled()) {
            log.debug("DataSource ({}) JDBC Connection [{}] will {}be managed by Spring", getDatasourceId(), connection, (isConnectionTransactional ? "" : "not "));
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection, SqlRequest sqlRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Releasing DataSource ({}) JDBC Connection [{}]", getDatasourceId(), connection);
        }
        try {
            DataSource dataSource = DataSourceHolder.isDynamicDataSourceReady() ?
                DataSourceHolder.currentDataSource().getNative() :
                this.dataSource;
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED, transactionManager = TransactionManagers.jdbcTransactionManager)
    public void execute(SqlRequest request) {
        super.execute(request);
    }

    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.jdbcTransactionManager)
    @Override
    public int update(SqlRequest request) {
        return super.update(request);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.jdbcTransactionManager)
    public <T, R> R select(SqlRequest request, ResultWrapper<T, R> wrapper) {
        return super.select(request, wrapper);
    }
}
