package org.hswebframework.web.dao.mybatis;

import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SQL;
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
    private DataSource dataSource;

    public DefaultJdbcExecutor(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);
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
