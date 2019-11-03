package org.hswebframework.web.cache;

public interface ReactiveCacheManager {

    <E> ReactiveCache<E> getCache(String name);
}
