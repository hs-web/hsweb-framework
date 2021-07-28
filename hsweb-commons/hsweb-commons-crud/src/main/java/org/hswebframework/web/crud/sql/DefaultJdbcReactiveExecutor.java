package org.hswebframework.web.crud.sql;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.jdbc.JdbcReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
public class DefaultJdbcReactiveExecutor extends JdbcReactiveSqlExecutor {
    @Autowired
    private DataSource dataSource;

    protected String getDatasourceId() {
        return DataSourceHolder.switcher().datasource().current().orElse("default");
    }

    private Tuple2<DataSource, Connection> getDataSourceAndConnection() {
        DataSource dataSource = DataSourceHolder.isDynamicDataSourceReady() ?
                DataSourceHolder.currentDataSource().getNative() :
                this.dataSource;
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean isConnectionTransactional = DataSourceUtils.isConnectionTransactional(connection, dataSource);
        if (log.isDebugEnabled()) {
            log.debug("DataSource ({}) JDBC Connection [{}] will {}be managed by Spring", getDatasourceId(), connection, (isConnectionTransactional ? "" : "not "));
        }
        return Tuples.of(dataSource, connection);
    }

    @Override
    public Mono<Connection> getConnection() {
        return Mono
                .using(
                        this::getDataSourceAndConnection
                        ,
                        tp2 -> Mono.just(tp2.getT2()),
                        tp2 -> DataSourceUtils.releaseConnection(tp2.getT2(), tp2.getT1()),
                        false
                );
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,readOnly = true)
    public <E> Flux<E> select(String sql, ResultWrapper<E, ?> wrapper) {
        return super.select(sql,wrapper);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,rollbackFor = Throwable.class)
    public Mono<Integer> update(Publisher<SqlRequest> request) {
        return super.update(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,rollbackFor = Throwable.class)
    public Mono<Integer> update(String sql, Object... args) {
        return super.update(sql,args);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,rollbackFor = Throwable.class)
    public Mono<Integer> update(SqlRequest request) {
        return super.update(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,rollbackFor = Throwable.class)
    public Mono<Void> execute(Publisher<SqlRequest> request) {
        return super.execute(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager,rollbackFor = Throwable.class)
    public Mono<Void> execute(SqlRequest request) {
        return super.execute(request);
    }
}
