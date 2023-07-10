package org.hswebframework.web.cache.supports;

import org.hswebframework.web.cache.ReactiveCache;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.core.publisher.Sinks;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class AbstractReactiveCache<E> implements ReactiveCache<E> {
    static Sinks.EmitFailureHandler emitFailureHandler = Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(30));
    private final Map<Object, CacheLoader> cacheLoading = new ConcurrentHashMap<>();

    protected static class CacheLoader extends MonoOperator<Object, Object> {

        private final AbstractReactiveCache<?> parent;

        private final Object key;
        private Mono<? extends Object> defaultValue;

        private final Sinks.One<Object> holder = Sinks.one();

        private volatile Disposable loading;

        protected CacheLoader(AbstractReactiveCache<?> parent, Object key, Mono<? extends Object> source) {
            super(source.cache());
            this.parent = parent;
            this.key = key;
        }

        protected void defaultValue(Mono<? extends Object> defaultValue, ContextView context) {
            if (this.defaultValue != null) {
                return;
            }
            this.defaultValue = defaultValue;
            tryLoad(context);
        }


        @SuppressWarnings("all")
        private void tryLoad(ContextView context) {
            if (holder.currentSubscriberCount() == 1 && loading == null) {
                Mono<? extends Object> source = this.source;
                if (defaultValue != null) {
                    source = source
                            .switchIfEmpty((Mono) defaultValue
                                    .flatMap(val -> {
                                        return parent.putNow(key, val).thenReturn(val);
                                    }));
                }
                loading = source.subscribe(
                        val -> {
                            complete();
                            holder.emitValue(val, emitFailureHandler);
                        },
                        err -> {
                            complete();
                            holder.emitError(err, emitFailureHandler);
                        },
                        () -> {
                            complete();
                            holder.emitEmpty(emitFailureHandler);
                        },
                        Context.of(context));
            }
        }

        @Override
        public void subscribe(CoreSubscriber<? super Object> actual) {
            holder.asMono().subscribe(actual);
            tryLoad(actual.currentContext());
        }

        private void complete() {
            parent.cacheLoading.remove(key, this);
        }


    }

    protected abstract Mono<Object> getNow(Object key);

    public abstract Mono<Void> putNow(Object key, Object value);

    @Override
    @SuppressWarnings("all")
    public final Mono<E> getMono(Object key) {
        return (Mono<E>) cacheLoading.computeIfAbsent(key, _key -> new CacheLoader(this, _key, getNow(_key)));
    }

    @Override
    @SuppressWarnings("all")
    public final Mono<E> getMono(Object key, Supplier<Mono<E>> loader) {

        return Mono.deferContextual(ctx -> {
            CacheLoader cacheLoader = cacheLoading.compute(key, (_key, old) -> {
                CacheLoader cl = new CacheLoader(this, _key, getNow(_key));
                cl.defaultValue(loader.get(), ctx);
                return cl;
            });
            return (Mono<E>) cacheLoader;
        });
    }


    @Override
    public final Flux<E> getFlux(Object key) {
        return (cacheLoading.computeIfAbsent(key, _key -> new CacheLoader(this, _key, getNow(_key))))
                .flatMapIterable(e -> ((List<E>) e));
    }

    @Override
    public final Flux<E> getFlux(Object key, Supplier<Flux<E>> loader) {
        return Flux.deferContextual(ctx -> {
            CacheLoader cacheLoader = cacheLoading.compute(key, (_key, old) -> {
                CacheLoader cl = new CacheLoader(this, _key, getNow(_key));
                cl.defaultValue(loader.get().collectList(), ctx);
                return cl;
            });
            return cacheLoader.flatMapIterable(e -> ((List<E>) e));
        });
    }


    @Override
    public final Mono<Void> put(Object key, Publisher<E> data) {

        if (data instanceof Mono) {
            return Mono.from(data)
                       .flatMap(e -> putNow(key, e));
        }
        return Flux.from(data)
                   .collectList()
                   .flatMap(e -> putNow(key, e));
    }

    @Override
    public abstract Mono<Void> evict(Object key);

    @Override
    public Flux<E> getAll(Object... keys) {
        return Flux.just(keys)
                   .flatMap(this::getMono);
    }

    @Override
    public abstract Mono<Void> evictAll(Iterable<?> key);

    @Override
    public abstract Mono<Void> clear();
}
