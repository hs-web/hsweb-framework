package org.hswebframework.web.crud.sql;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.reactive.r2dbc.R2dbcReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.R2dbcDataSource;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.connectionfactory.ConnectionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

public class DefaultR2dbcExecutor extends R2dbcReactiveSqlExecutor {

    @Autowired
    private ConnectionFactory defaultFactory;

    @Setter
    private boolean bindCustomSymbol = false;

    @Setter
    private String bindSymbol = "$";

    @Override
    public String getBindSymbol() {
        return bindSymbol;
    }

    @Override
    protected SqlRequest convertRequest(SqlRequest sqlRequest) {
        if (bindCustomSymbol) {
            return super.convertRequest(sqlRequest);
        }
        return sqlRequest;
    }

    protected void bindNull(Statement statement, int index, Class type) {
        if (bindCustomSymbol) {
            statement.bindNull(getBindSymbol() + (index + getBindFirstIndex()), type);
            return;
        }
        statement.bindNull(index, type);
    }

    protected void bind(Statement statement, int index, Object value) {
        if (value instanceof Date) {
            value = ((Date) value)
                    .toInstant()
                    .atZone(ZoneOffset.systemDefault())
                    .toLocalDateTime();
        }
        if (bindCustomSymbol) {
            statement.bind(getBindSymbol() + (index + getBindFirstIndex()), value);
            return;
        }
        statement.bind(index, value);
    }

    @Override
    protected Mono<Connection> getConnection() {
        if (DataSourceHolder.isDynamicDataSourceReady()) {
            return DataSourceHolder.currentR2dbc()
                    .flatMap(R2dbcDataSource::getNative)
                    .flatMap(ConnectionFactoryUtils::getConnection);
        } else {
            return ConnectionFactoryUtils.getConnection(defaultFactory);
        }
    }

    @Override
    protected void releaseConnection(SignalType type, Connection connection) {
        //所有方法都被事务接管,不用手动释放
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Void> execute(SqlRequest request) {
        return super.execute(request);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Void> execute(Publisher<SqlRequest> request) {
        return super.execute(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Integer> update(Publisher<SqlRequest> request) {
        return super.update(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Integer> update(SqlRequest request) {
        return super.update(request);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Mono<Integer> update(String sql, Object... args) {
        return super.update(sql,args);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public <E> Flux<E> select(Publisher<SqlRequest> request, ResultWrapper<E, ?> wrapper) {
        return super.select(request, wrapper);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public Flux<Map<String, Object>> select(String sql, Object... args) {
        return super.select(sql,args);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public <E> Flux<E> select(String sql, ResultWrapper<E, ?> wrapper) {
        return super.select(sql,wrapper);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TransactionManagers.r2dbcTransactionManager)
    public <E> Flux<E> select(SqlRequest sqlRequest, ResultWrapper<E, ?> wrapper) {
        return super.select(sqlRequest,wrapper);
    }
}
