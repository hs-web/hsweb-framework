package org.hswebframework.web.crud.sql;

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.jdbc.JdbcReactiveSqlExecutor;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;

public class DefaultJdbcReactiveExecutor extends JdbcReactiveSqlExecutor {
    @Autowired
    private DataSource dataSource;

    @Override
    public Mono<Connection> getConnection(SqlRequest sqlRequest) {

        DataSource dataSource = DataSourceHolder.isDynamicDataSourceReady() ?
                DataSourceHolder.currentDataSource().getNative() :
                this.dataSource;
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return Mono.just(connection);

    }

    @Override
    public void releaseConnection(Connection connection, SqlRequest sqlRequest) {
        DataSource dataSource = DataSourceHolder.isDynamicDataSourceReady() ?
                DataSourceHolder.currentDataSource().getNative() :
                this.dataSource;
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
