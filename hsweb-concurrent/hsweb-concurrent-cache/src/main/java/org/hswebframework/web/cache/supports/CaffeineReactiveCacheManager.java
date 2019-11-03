package org.hswebframework.web.cache.supports;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import org.hswebframework.web.cache.ReactiveCache;

import java.time.Duration;

@AllArgsConstructor
public class CaffeineReactiveCacheManager extends AbstractReactiveCacheManager {

    private Caffeine<Object, Object> builder;


    @Override
    protected <E> ReactiveCache<E> createCache(String name) {
        return new CaffeineReactiveCache<>(builder.build());
    }

}
