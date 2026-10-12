package org.hswebframework.web.crud;

import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/** 只记录交给真实执行器的写 SQL；不模拟 SQL、事务或实体事件。 */
public final class CountingReactiveSqlExecutor implements ReactiveSqlExecutor, AutoCloseable {
    private final RDBTableMetadata table;
    private final ReactiveSqlExecutor delegate;
    private final Feature previous;
    private final List<String> statements = new CopyOnWriteArrayList<>();

    public CountingReactiveSqlExecutor(RDBTableMetadata table) {
        this.table = table;
        this.previous = table.getFeatures().get(ID_VALUE);
        this.delegate = table.findFeatureNow(ID);
        table.addFeature(this);
    }

    public void reset() {
        statements.clear();
    }

    public long count(String verb) {
        String prefix = verb.toLowerCase(Locale.ROOT) + " ";
        return statements.stream().filter(sql -> sql.trim().toLowerCase(Locale.ROOT).startsWith(prefix)).count();
    }

    @Override
    public Mono<Integer> update(Publisher<SqlRequest> requests) {
        return delegate.update(Flux.from(requests).doOnNext(request -> statements.add(request.getSql())));
    }

    @Override
    public Mono<Void> execute(Publisher<SqlRequest> requests) {
        return delegate.execute(requests);
    }

    @Override
    public <E> Flux<E> select(Publisher<SqlRequest> requests, ResultWrapper<E, ?> wrapper) {
        return delegate.select(requests, wrapper);
    }

    @Override
    public void close() {
        if (previous == null) {
            table.getFeatures().remove(ID_VALUE);
        } else {
            table.addFeature(previous);
        }
    }
}
