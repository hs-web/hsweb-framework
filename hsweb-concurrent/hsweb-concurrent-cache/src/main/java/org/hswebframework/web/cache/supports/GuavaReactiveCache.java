package org.hswebframework.web.cache.supports;

import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Guava 的响应式适配，存储操作同步执行，以在途加载者身份保护本地失效。
 */
@SuppressWarnings("all")
@AllArgsConstructor
public class GuavaReactiveCache<E> extends AbstractReactiveCache<E> {

    private Cache<Object, Object> cache;


    @Override
    public Mono<Void> evictAll(Iterable<?> key) {
        return Mono.fromRunnable(() -> key.forEach(k -> invalidateLoading(k, () -> cache.invalidate(k))));
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
        return Mono.fromRunnable(() -> invalidateLoading(key, () -> cache.invalidate(key)));
    }

    @Override
    protected Mono<Void> evictLoading(Object key, CacheLoader owner) {
        return Mono.fromRunnable(() -> invalidateLoading(key, owner, () -> cache.invalidate(key)));
    }

    @Override
    public Flux<E> getAll(Object... keys) {
        return Flux.<E>defer(() -> {
            if (keys == null || keys.length == 0) {
                return Flux
                        .fromIterable(cache.asMap().values())
                        .map(e -> (E) e);
            }
            return Flux.fromIterable(cache.getAllPresent(Arrays.asList(keys)).values())
                       .map(e -> (E) e);
        });
    }


    @Override
    public Mono<Void> clear() {
        return Mono.fromRunnable(() -> invalidateAllLoading(cache::invalidateAll));
    }
}
