package org.hswebframework.web.cache.supports;

import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.cache.ReactiveCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractReactiveCacheManager implements ReactiveCacheManager {
    private Map<String, ReactiveCache> caches = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("all")
    public <E> ReactiveCache<E> getCache(String name) {
        return caches.computeIfAbsent(name, this::createCache);
    }

    protected abstract <E> ReactiveCache<E> createCache(String name);
}
