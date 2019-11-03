package org.hswebframework.web.cache.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.cache.ReactiveCache;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.cache.supports.CaffeineReactiveCacheManager;
import org.hswebframework.web.cache.supports.GuavaReactiveCacheManager;
import org.hswebframework.web.cache.supports.RedisLocalReactiveCacheManager;
import org.hswebframework.web.cache.supports.UnSupportedReactiveCache;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;

@ConfigurationProperties(prefix = "hsweb.cache")
@Getter
@Setter
public class ReactiveCacheProperties {


    private Type type = Type.none;

    private GuavaProperties guava = new GuavaProperties();

    private CaffeineProperties caffeine = new CaffeineProperties();

    private RedisProperties redis = new RedisProperties();


    public boolean anyProviderPresent() {
        return ClassUtils.isPresent("com.google.common.cache.Cache", this.getClass().getClassLoader())
                || ClassUtils.isPresent("com.github.benmanes.caffeine.cache.Cache", this.getClass().getClassLoader())
                || ClassUtils.isPresent("org.springframework.data.redis.core.ReactiveRedisOperations", this.getClass().getClassLoader());
    }


    private ReactiveCacheManager createUnsupported() {
        return new ReactiveCacheManager() {
            @Override
            public <E> ReactiveCache<E> getCache(String name) {
                return UnSupportedReactiveCache.getInstance();
            }
        };
    }

    @SuppressWarnings("all")
    public ReactiveCacheManager createCacheManager(ApplicationContext context) {
        if (!anyProviderPresent()) {
            return new ReactiveCacheManager() {
                @Override
                public <E> ReactiveCache<E> getCache(String name) {
                    return UnSupportedReactiveCache.getInstance();
                }
            };
        }

        if (type == Type.redis) {
            ReactiveRedisOperations<Object, Object> operations;
            if (StringUtils.hasText(redis.getBeanName())) {
                operations = context.getBean(redis.getBeanName(), ReactiveRedisOperations.class);
            } else {
                operations = (ReactiveRedisOperations) context.getBeanProvider(ResolvableType.forClassWithGenerics(ReactiveRedisOperations.class, Object.class, Object.class)).getIfAvailable();
            }
            return new RedisLocalReactiveCacheManager(operations, createCacheManager(type));
        }

        return createCacheManager(type);
    }

    private ReactiveCacheManager createCacheManager(Type type) {
        switch (type) {
            case guava:
                return getGuava().createCacheManager();
            case caffeine:
                return getCaffeine().createCacheManager();

        }
        return createUnsupported();
    }


    @Getter
    @Setter
    public static class RedisProperties {
        private String beanName;

        private Type localCacheType = Type.caffeine;

    }

    @Getter
    @Setter
    public static class GuavaProperties {
        long maximumSize = 1024;
        int initialCapacity = 64;
        Duration expireAfterWrite = Duration.ofHours(6);
        Duration expireAfterAccess = Duration.ofHours(1);
        Strength keyStrength = Strength.SOFT;
        Strength valueStrength = Strength.SOFT;

        ReactiveCacheManager createCacheManager() {
            return new GuavaReactiveCacheManager(createBuilder());
        }

        CacheBuilder<Object, Object> createBuilder() {
            CacheBuilder builder = CacheBuilder.newBuilder()
                    .expireAfterAccess(expireAfterAccess)
                    .expireAfterWrite(expireAfterWrite)
                    .maximumSize(maximumSize);
            if (valueStrength == Strength.SOFT) {
                builder.softValues();
            } else {
                builder.weakValues();
            }
            if (keyStrength == Strength.WEAK) {
                builder.weakKeys();
            }
            return builder;
        }
    }

    @Getter
    @Setter
    public static class CaffeineProperties {
        long maximumSize = 1024;
        int initialCapacity = 64;
        Duration expireAfterWrite = Duration.ofHours(6);
        Duration expireAfterAccess = Duration.ofHours(1);
        Strength keyStrength = Strength.SOFT;
        Strength valueStrength = Strength.SOFT;

        ReactiveCacheManager createCacheManager() {
            return new CaffeineReactiveCacheManager(createBuilder());
        }

        Caffeine<Object, Object> createBuilder() {
            Caffeine builder = Caffeine.newBuilder()
                    .expireAfterAccess(expireAfterAccess)
                    .expireAfterWrite(expireAfterWrite)
                    .maximumSize(maximumSize);
            if (valueStrength == Strength.SOFT) {
                builder.softValues();
            } else {
                builder.weakValues();
            }
            if (keyStrength == Strength.WEAK) {
                builder.weakKeys();
            }
            return builder;
        }
    }

    enum Strength {WEAK, SOFT}

    public enum Type {
        redis,
        caffeine,
        guava,
        none,
    }

}
