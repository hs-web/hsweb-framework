package org.hswebframework.web.crud.sql;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.reactive.r2dbc.R2dbcReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.web.api.crud.entity.TransactionManagers;
import org.hswebframework.web.exception.I18nSupportException;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * 使用 Spring 管理连接和事务的响应式 SQL 执行器。
 * 位置模式保留原生占位符；名称模式按配置改写并绑定普通参数，保留具名参数的重复引用。
 */
public class DefaultR2dbcExecutor extends R2dbcReactiveSqlExecutor {

    @Autowired
    @Setter
    private ConnectionFactory defaultFactory;

    @Setter
    // 同时控制普通参数的名称绑定；关闭时使用原有 SPI 位置绑定。
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

    @Override
    protected Statement prepareStatement(Statement statement, SqlRequest request) {
        try {
            return super.prepareStatement(statement, request);
        } catch (Throwable e) {
            throw new I18nSupportException
                .NoStackTrace("error.sql.prepare", e)
                .withSource("sql.prepare", request);
        }
    }

    protected void bindNull(Statement statement, int index, Class type) {
        if (bindCustomSymbol) {
            super.bindNull(statement, index, type);
            return;
        }
        if (type == Date.class) {
            type = LocalDateTime.class;
        }
        statement.bindNull(index, type);
    }

    protected void bind(Statement statement, int index, Object value) {
        if (bindCustomSymbol) {
            super.bind(statement, index, value);
            return;
        }
        if (value instanceof Date) {
            value = ((Date) value)
                .toInstant()
                .atZone(ZoneOffset.systemDefault())
                .toLocalDateTime();
        }
        statement.bind(index, value);
    }

    @Override
    protected Mono<Connection> getConnection() {
        return ConnectionFactoryUtils
            .getConnection(defaultFactory);
    }

    @Override
    protected <T> Flux<T> doInConnection(Function<Connection, Publisher<T>> handler) {
        Mono<ConnectionCloseHolder> connectionMono = getConnection().map(
            connection -> new ConnectionCloseHolder(connection, this::closeConnection));

        return Flux.usingWhen(
            connectionMono,
            holder -> handler.apply(holder.connection),
            ConnectionCloseHolder::close,
            (it, err) -> it.close(),
            ConnectionCloseHolder::close
        );

        // return super.doWith(handler);
    }

    static class ConnectionCloseHolder extends AtomicBoolean {

        @Serial
        private static final long serialVersionUID = -8994138383301201380L;

        final transient Connection connection;

        final transient Function<Connection, Publisher<Void>> closeFunction;

        ConnectionCloseHolder(Connection connection, Function<Connection, Publisher<Void>> closeFunction) {
            this.connection = connection;
            this.closeFunction = closeFunction;
        }

        Mono<Void> close() {
            return Mono.defer(() -> {
                if (compareAndSet(false, true)) {
                    return Mono.from(this.closeFunction.apply(this.connection));
                }
                return Mono.empty();
            });
        }
    }

    private Publisher<Void> closeConnection(Connection connection) {
        return ConnectionFactoryUtils
            .currentConnectionFactory(defaultFactory).then()
            .onErrorResume(Exception.class, ex -> Mono.from(connection.close()));
    }

    @Override
    protected void releaseConnection(SignalType type, Connection connection) {
        //所有方法都被事务接管,不用手动释放
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Void> execute(SqlRequest request) {
        return super.execute(request);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Void> execute(Publisher<SqlRequest> request) {
        return super.execute(request);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Integer> update(Publisher<SqlRequest> request) {
        return super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Integer> update(SqlRequest request) {
        return super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, transactionManager = TransactionManagers.reactiveTransactionManager)
    public Mono<Integer> update(String sql, Object... args) {
        return super.update(sql, args);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    public <E> Flux<E> select(Publisher<SqlRequest> request, ResultWrapper<E, ?> wrapper) {
        return super.select(request, wrapper);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    public Flux<Map<String, Object>> select(String sql, Object... args) {
        return super.select(sql, args);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    public <E> Flux<E> select(String sql, ResultWrapper<E, ?> wrapper) {
        return super.select(sql, wrapper);
    }

    @Override
    @Transactional(transactionManager = TransactionManagers.reactiveTransactionManager)
    public <E> Flux<E> select(SqlRequest sqlRequest, ResultWrapper<E, ?> wrapper) {
        return super.select(sqlRequest, wrapper);
    }
}
