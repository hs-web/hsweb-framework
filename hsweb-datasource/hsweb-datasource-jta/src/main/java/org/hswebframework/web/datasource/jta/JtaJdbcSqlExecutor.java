package org.hswebframework.web.datasource.jta;

import org.hswebframework.ezorm.rdb.executor.SQL;
import org.hswebframework.web.datasource.DefaultJdbcExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 支持JTA事务的sql执行器
 *
 * @author zhouhao
 * @since 3.0
 */
@Transactional(rollbackFor = Throwable.class)
public class JtaJdbcSqlExecutor extends DefaultJdbcExecutor {
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
