package org.hswebframework.web.cache.supports;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import org.hswebframework.web.cache.ReactiveCache;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("all")
@AllArgsConstructor
public class CaffeineReactiveCache<E> extends AbstractReactiveCache<E> {

    private Cache<Object, Object> cache;

    @Override
    public Mono<Void> evictAll(Iterable<?> key) {
        return Mono.fromRunnable(() -> cache.invalidateAll(key));
    }

    @Override
    public Flux<E> getAll(Object... keys) {
        return Flux.<E>defer(() -> {
            if (keys == null || keys.length == 0) {
                return Flux.fromIterable(cache.asMap().values())
                           .map(e -> (E) e);
            }
            return Flux.fromIterable(cache.getAllPresent(Arrays.asList(keys)).values())
                       .map(e -> (E) e);
        });
    }

    @Override
    protected Mono<Object> getNow(Object key) {
        return Mono.justOrEmpty(cache.getIfPresent(key));
    }

    @Override
    public Mono<Void> putNow(Object key, Object value) {
        cache.put(key, value);
        return Mono.empty();
    }

    @Override
    public Mono<Void> evict(Object key) {
        return Mono.fromRunnable(() -> cache.invalidate(key));
    }

    @Override
    public Mono<Void> clear() {
        return Mono.fromRunnable(() -> cache.invalidateAll());
    }
}
