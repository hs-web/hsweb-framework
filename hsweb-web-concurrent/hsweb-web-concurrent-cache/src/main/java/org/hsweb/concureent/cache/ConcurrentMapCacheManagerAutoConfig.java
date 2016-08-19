package org.hsweb.concureent.cache;

import org.hsweb.concureent.cache.monitor.SimpleMonitorCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

@EnableCaching
@Configuration
@ConditionalOnMissingClass(value = {"org.springframework.data.redis.connection.jedis.JedisConnection"})
public class ConcurrentMapCacheManagerAutoConfig extends CachingConfigurerSupport {
    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager() {
            @Override
            protected Cache getMissingCache(String name) {
                return new SimpleMonitorCache(name);
            }
        };
        cacheManager.setCaches(new HashSet<>());
        return cacheManager;
    }
}