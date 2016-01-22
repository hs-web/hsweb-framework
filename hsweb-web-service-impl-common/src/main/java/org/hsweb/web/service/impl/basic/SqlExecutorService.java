package org.hsweb.web.service.impl.basic;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.webbuilder.sql.support.executor.AbstractJdbcSqlExecutor;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * SQL执行服务类，用于执行原生sql
 * Created by 浩 on 2015-10-09 0009.
 */
@Service
public class SqlExecutorService extends AbstractJdbcSqlExecutor {

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
}
