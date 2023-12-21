package org.hswebframework.web.cache;

import org.reactivestreams.Publisher;
import reactor.cache.CacheFlux;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 响应式缓存
 *
 * @param <E> 缓存元素类型
 */
public interface ReactiveCache<E> {

    Flux<E> getFlux(Object key);

    Flux<E> getFlux(Object key, Supplier<Flux<E>> loader);

    Mono<E> getMono(Object key);

    Mono<E> getMono(Object key, Supplier<Mono<E>> loader);

    Mono<Void> put(Object key, Publisher<E> data);

    Mono<Void> evict(Object key);

    Flux<E> getAll(Object... keys);

    Mono<Void> evictAll(Iterable<?> key);

    Mono<Void> clear();

    /**
     * @deprecated <a href="https://github.com/reactor/reactor-addons/issues/237">https://github.com/reactor/reactor-addons/issues/237</a>
     */
    @Deprecated
    default CacheFlux.FluxCacheBuilderMapMiss<E> flux(Object key) {
        return otherSupplier -> Flux
                .defer(() -> this
                        .getFlux(key)
                        .switchIfEmpty(otherSupplier.get()
                                                    .collectList()
                                                    .flatMapMany(values -> put(key, Flux.fromIterable(values))
                                                            .thenMany(Flux.fromIterable(values)))));
    }

    /**
     * @deprecated <a href="https://github.com/reactor/reactor-addons/issues/237">https://github.com/reactor/reactor-addons/issues/237</a>
     */
    @Deprecated
    default CacheMono.MonoCacheBuilderMapMiss<E> mono(Object key) {
        return otherSupplier -> Mono
                .defer(() -> this
                        .getMono(key)
                        .switchIfEmpty(otherSupplier.get()
                                                    .flatMap(value -> put(key, Mono.just(value)).thenReturn(value))));
    }
}
