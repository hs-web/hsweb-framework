package org.hsweb.concureent.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

@EnableCaching
@Configuration
@ConditionalOnMissingClass(value = {"org.springframework.data.redis.connection.jedis.JedisConnection"})
public class ConcurrentMapCacheManagerAutoConfig extends CachingConfigurerSupport {
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                if (obj == null) obj = "null";
                sb.append(obj.hashCode());
            }
            return sb.toString();
        };
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager() {

            @Override
            protected Cache getMissingCache(String name) {
                return new ConcurrentMapCache(name);
            }
        };
        cacheManager.setCaches(new HashSet<>());
        return cacheManager;
    }
}