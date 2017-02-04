/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.concureent.cache.monitor;

import static org.springframework.util.Assert.*;
import static org.springframework.util.ObjectUtils.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.hsweb.commons.StringUtils;
import org.hsweb.web.core.cache.monitor.MonitorCache;
import org.hsweb.web.core.utils.ThreadLocalUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ClassUtils;

/**
 * Cache implementation on top of Redis.
 *
 * @author Costin Leau
 * @author Christoph Strobl
 * @author Thomas Darimont
 */
@SuppressWarnings("unchecked")
public class RedisMonitorCache extends RedisCache implements Cache, MonitorCache {

    @SuppressWarnings("rawtypes")//
    private final RedisOperations redisOperations;
    private final byte[]          totalTimeKey;
    private final byte[]          hitTimeKey;
    private final byte[]          putTimeKey;
    private final byte[]          keySetKey;
    private long expiration = 0;

    public RedisMonitorCache(String name, byte[] prefix, RedisOperations<? extends Object, ? extends Object> redisOperations,
                             long expiration) {
        super(name, prefix, redisOperations, expiration);
        this.expiration = expiration;
        this.redisOperations = redisOperations;
        this.keySetKey = (name + "~keys").getBytes();
        this.totalTimeKey = name.concat(":total-times").getBytes();
        this.hitTimeKey = name.concat(":hit-times").getBytes();
        this.putTimeKey = name.concat(":put-times").getBytes();
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        String localCacheKey = "cache-".concat(String.valueOf(key));
        T localCache = ThreadLocalUtils.get(localCacheKey);
        if (localCache != null) {
            return localCache;
        }
        T v = super.get(key, type);
        redisOperations.execute((RedisCallback) connection -> {
            connection.incr(totalTimeKey);
            if (v != null) {
                connection.incr(hitTimeKey);
            }
            return null;
        });
        if (v != null) {
            ThreadLocalUtils.put(localCacheKey, v);
        }
        return v;
    }

    @Override
    public ValueWrapper get(Object key) {
        String localCacheKey = "cache-".concat(String.valueOf(key));
        ValueWrapper localCache = ThreadLocalUtils.get(localCacheKey);
        if (localCache != null) {
            return localCache;
        }
        ValueWrapper wrapper = super.get(key);
        redisOperations.execute((RedisCallback) connection -> {
            connection.incr(totalTimeKey);
            if (wrapper != null) {
                connection.incr(hitTimeKey);
            }
            return null;
        });
        if (wrapper != null) {
            ThreadLocalUtils.put(localCacheKey, wrapper);
        }
        return wrapper;
    }

    @Override
    public void put(Object key, Object value) {
        super.put(key, value);
        redisOperations.execute((RedisCallback) connection -> {
            connection.multi();
            connection.incr(putTimeKey);
            connection.sAdd(keySetKey, ((String) key).getBytes());
            if (expiration != 0) connection.expire(keySetKey, expiration);
            connection.exec();
            return null;
        });
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        redisOperations.execute((RedisCallback) connection -> {
            connection.sRem(keySetKey, ((String) key).getBytes());
            return null;
        });
    }

    @Override
    public void clear() {
        super.clear();
        redisOperations.delete(new String(keySetKey));
    }

    @Override
    public Set<Object> keySet() {
        return (Set<Object>) redisOperations.execute((RedisCallback) connection -> connection.sMembers(keySetKey).stream().map(String::new).collect(Collectors.toSet()));
    }

    @Override
    public int size() {
        return redisOperations.opsForSet().size(new String(keySetKey)).intValue();
    }

    @Override
    public long getTotalTimes() {
        return StringUtils.toInt(redisOperations.opsForValue().get(new String(totalTimeKey)));
    }

    @Override
    public long getHitTimes() {
        return StringUtils.toInt(redisOperations.opsForValue().get(new String(hitTimeKey)));
    }

    @Override
    public long getPutTimes() {
        return StringUtils.toInt(redisOperations.opsForValue().get(new String(putTimeKey)));
    }
}
