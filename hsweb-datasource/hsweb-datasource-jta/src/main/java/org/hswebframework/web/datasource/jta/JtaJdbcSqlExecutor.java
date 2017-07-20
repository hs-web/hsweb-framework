package org.hswebframework.web.datasource.jta;

import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SQL;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 支持JTA事务的sql执行器
 *
 * @author zhouhao
 * @since 3.0
 */
@Transactional(rollbackFor = Throwable.class)
public class JtaJdbcSqlExecutor extends AbstractJdbcSqlExecutor {
    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(DataSourceHolder.currentDataSource().getNative());
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        DataSourceUtils.releaseConnection(connection, DataSourceHolder.currentDataSource().getNative());
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
