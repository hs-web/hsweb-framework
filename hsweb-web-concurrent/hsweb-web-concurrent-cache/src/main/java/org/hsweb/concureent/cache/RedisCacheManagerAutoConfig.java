package org.hsweb.concureent.cache;

import org.hsweb.concureent.cache.monitor.RedisMonitorCache;
import org.hsweb.concureent.cache.redis.FastJsonRedisTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import redis.clients.jedis.Jedis;


/**
 * Created by zhouhao on 16-4-26.
 */
@EnableCaching
@Configuration
@ConditionalOnClass({JedisConnection.class, RedisOperations.class, Jedis.class})
public class RedisCacheManagerAutoConfig extends CachingConfigurerSupport {

    @Bean
    @ConditionalOnMissingBean(FastJsonRedisTemplate.class)
    public FastJsonRedisTemplate redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        FastJsonRedisTemplate template = new FastJsonRedisTemplate(redisConnectionFactory);
        return template;
    }

    @Bean
    public CacheManager cacheManager(FastJsonRedisTemplate redisTemplate) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate){
            @Override
            protected RedisCache createCache(String cacheName) {
                long expiration = computeExpiration(cacheName);
                return new RedisMonitorCache(cacheName, new DefaultRedisCachePrefix().prefix(cacheName), redisTemplate, expiration);
            }
        };
        redisCacheManager.setUsePrefix(true);
        return redisCacheManager;
    }

    @Bean
    public KeyGenerator wiselyKeyGenerator() {
        return new SimpleKeyGenerator();
    }


}