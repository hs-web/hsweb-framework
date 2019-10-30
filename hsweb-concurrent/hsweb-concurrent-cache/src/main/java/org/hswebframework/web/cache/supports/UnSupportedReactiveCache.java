package org.hswebframework.web.cache.supports;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hswebframework.web.cache.ReactiveCache;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnSupportedReactiveCache<E> implements ReactiveCache<E> {

    private static final UnSupportedReactiveCache INSTANCE = new UnSupportedReactiveCache();

    public static <E> ReactiveCache<E> getInstance() {
        return INSTANCE;
    }

    @Override
    public Flux<E> getFlux(Object key) {
        return Flux.empty();
    }

    @Override
    public Mono<E> getMono(Object key) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> put(Object key, Publisher<E> data) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> evict(Object key) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> clear() {
        return Mono.empty();
    }
}
