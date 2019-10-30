package org.hswebframework.web.cache.supports;

import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import org.hswebframework.web.cache.ReactiveCache;

import java.time.Duration;

@AllArgsConstructor
public class GuavaReactiveCacheManager extends AbstractReactiveCacheManager {

    private CacheBuilder<Object, Object> builder;

    @Override
    protected <E> ReactiveCache<E> createCache(String name) {
        return new GuavaReactiveCache<>(builder.build());
    }

}
