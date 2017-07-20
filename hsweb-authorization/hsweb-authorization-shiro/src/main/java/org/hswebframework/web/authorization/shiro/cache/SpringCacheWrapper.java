/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SuppressWarnings("unchecked")
public class SpringCacheWrapper<K, V> implements Cache<K, V> {
    private org.springframework.cache.Cache springCache;

    public SpringCacheWrapper(org.springframework.cache.Cache springCache) {
        this.springCache = springCache;
    }

    @Override
    public V get(K key) throws CacheException {
        return (V) springCache.get(key);
    }

    @Override
    public V put(K key, V value) throws CacheException {
        springCache.put(key, value);
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        V old = get(key);
        springCache.evict(key);
        return old;
    }

    @Override
    public void clear() throws CacheException {
        springCache.clear();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}
