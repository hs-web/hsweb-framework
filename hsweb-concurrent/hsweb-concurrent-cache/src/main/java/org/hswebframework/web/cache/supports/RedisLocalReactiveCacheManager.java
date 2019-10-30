package org.hswebframework.web.cache.supports;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.springframework.data.redis.core.ReactiveRedisOperations;

public class RedisLocalReactiveCacheManager extends AbstractReactiveCacheManager {

    private ReactiveRedisOperations<Object, Object> operations;

    private ReactiveCacheManager localCacheManager;

    public RedisLocalReactiveCacheManager(ReactiveRedisOperations<Object, Object> operations, ReactiveCacheManager localCacheManager) {
        this.operations = operations;
        this.localCacheManager = localCacheManager;
    }

    @Setter
    @Getter
    private String redisCachePrefix = "spring-cache:";

    @Override
    protected <E> ReactiveCache<E> createCache(String name) {
        return new RedisReactiveCache<>(redisCachePrefix.concat(name), operations, localCacheManager.getCache(name));
    }
}
