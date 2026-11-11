package org.hswebframework.web.crud;

import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.core.meta.FeatureSupportedMetadata;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/** 装饰指定元数据作用域的真实执行器并记录 SQL；查询计数应使用数据库作用域。 */
public final class CountingReactiveSqlExecutor implements ReactiveSqlExecutor, AutoCloseable {
    private final FeatureSupportedMetadata metadata;
    private final ReactiveSqlExecutor delegate;
    private final Feature previous;
    private final List<String> statements = new CopyOnWriteArrayList<>();

    public CountingReactiveSqlExecutor(FeatureSupportedMetadata metadata) {
        this.metadata = metadata;
        this.previous = metadata.getFeatures().get(ID_VALUE);
        this.delegate = metadata.findFeatureNow(ID);
        metadata.addFeature(this);
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
        return delegate.select(Flux.from(requests).doOnNext(request -> statements.add(request.getSql())), wrapper);
    }

    @Override
    public void close() {
        if (previous == null) {
            metadata.getFeatures().remove(ID_VALUE);
        } else {
            metadata.addFeature(previous);
        }
    }
}
