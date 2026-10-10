package org.hswebframework.web.cache.supports;

import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 管理响应式缓存的在途加载与共享结果，存储操作由具体后端实现。
 * 本地同步后端以加载者身份保护回填和失败清理，不提供异步远程后端的原子保证。
 */
@Slf4j
public abstract class AbstractReactiveCache<E> implements ReactiveCache<E> {
    static final Sinks.EmitFailureHandler emitFailureHandler = Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(30));

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
                                return parent.putLoadedValue(this, val).thenReturn(val);
                            }));
                }
                // 在 complete 撤销身份前清理失败，避免旧加载失败误删新加载者或新值。
                source = source.onErrorResume(err -> parent.handleLoaderError(
                    key, err, parent.evictLoading(key, this)));
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

    private Mono<Void> putLoadedValue(CacheLoader owner, Object value) {
        AtomicReference<Mono<Void>> write = new AtomicReference<>(Mono.empty());
        cacheLoading.computeIfPresent(owner.key, (key, current) -> {
            if (current == owner) {
                // 本地缓存同步写入，与同一 key 的失效共用加载映射的原子操作。
                write.set(putNow(key, value));
            }
            return current;
        });
        return write.get();
    }

    protected final void invalidateLoading(Object key, Runnable invalidation) {
        cacheLoading.compute(key, (_key, current) -> {
            invalidation.run();
            return null;
        });
    }

    protected final void invalidateLoading(Object key, CacheLoader expected, Runnable invalidation) {
        cacheLoading.computeIfPresent(key, (_key, current) -> {
            if (current == expected) {
                invalidation.run();
                return null;
            }
            return current;
        });
    }

    protected final void invalidateAllLoading(Runnable invalidation) {
        // 撤销处理时已登记的加载者；与 clear 重叠的新订阅不属于全缓存事务。
        cacheLoading.clear();
        invalidation.run();
    }

    private CacheLoader createLoader(Object key) {
        // 读取也延迟到订阅，不能在装配时捕获失效前的本地值。
        return new CacheLoader(this, key, Mono.defer(() -> getNow(key)));
    }

    @Override
    @SuppressWarnings("all")
    public final Mono<E> getMono(Object key) {
        return Mono.defer(() -> (Mono<E>) cacheLoading.computeIfAbsent(key, this::createLoader))
            .onErrorResume(err -> handleLoaderError(key, err));
    }

    @Override
    @SuppressWarnings("all")
    public final Mono<E> getMono(Object key, Supplier<Mono<E>> loader) {

        return Mono
            .deferContextual(ctx -> {
                CacheLoader cacheLoader = cacheLoading.compute(key, (_key, old) -> {
                    CacheLoader cl = createLoader(_key);
                    cl.defaultValue(loader.get(), ctx);
                    return cl;
                });
                return (Mono<E>) cacheLoader;
            })
            .onErrorResume(err -> handleLoaderError(key, err));
    }


    @Override
    public final Flux<E> getFlux(Object key) {
        return Flux.defer(() -> cacheLoading.computeIfAbsent(key, this::createLoader)
                                           .flatMapIterable(e -> ((List<E>) e)))
            .onErrorResume(err -> handleLoaderError(key, err));
    }

    @Override
    public final Flux<E> getFlux(Object key, Supplier<Flux<E>> loader) {
        return Flux.deferContextual(ctx -> {
                       CacheLoader cacheLoader = cacheLoading.compute(key, (_key, old) -> {
                           CacheLoader cl = createLoader(_key);
                           cl.defaultValue(loader.get().collectList(), ctx);
                           return cl;
                       });
                       return cacheLoader.flatMapIterable(e -> ((List<E>) e));
                   })
                   .onErrorResume(err -> handleLoaderError(key, err));
    }

    protected Mono<E> handleLoaderError(Object key, Throwable err) {
        return handleLoaderError(key, err, evict(key));
    }

    protected Mono<Void> evictLoading(Object key, CacheLoader owner) {
        // 异步后端保留原失效逻辑；本地后端在同一原子操作中校验身份和清理。
        return evict(key);
    }

    private <V> Mono<V> handleLoaderError(Object key, Throwable err, Mono<Void> cleanup) {
        log.warn("load cache error,key:{}.", key, err);
        return cleanup.then(Mono.empty());
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
