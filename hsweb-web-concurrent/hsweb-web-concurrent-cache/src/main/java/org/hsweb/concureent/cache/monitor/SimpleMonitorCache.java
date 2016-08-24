/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.concureent.cache.monitor;

import org.hsweb.web.core.cache.monitor.MonitorCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleMonitorCache extends ConcurrentMapCache implements MonitorCache {

    private final AtomicInteger totalTimes = new AtomicInteger(0);
    private final AtomicInteger hitTimes = new AtomicInteger(0);
    private final AtomicInteger putTimes = new AtomicInteger(0);

    public SimpleMonitorCache(String name) {
        super(name, false);
    }

    @Override
    public Set<Object> keySet() {
        return getNativeCache().keySet();
    }

    @Override
    public int size() {
        return getNativeCache().size();
    }

    @Override
    public long getTotalTimes() {
        return totalTimes.get();
    }

    @Override
    public long getHitTimes() {
        return hitTimes.get();
    }

    @Override
    protected Object lookup(Object key) {
        Object value = super.lookup(key);
        if (value != null && value instanceof Reference) {
            Reference reference = (Reference) value;
            value = reference.get();
            if (value == null)
                evict(key);
        }
        return value;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = super.get(key);
        totalTimes.addAndGet(1);
        if (wrapper != null) {
            hitTimes.addAndGet(1);
        }
        return wrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = super.get(key, type);
        totalTimes.addAndGet(1);
        if (value != null) {
            hitTimes.addAndGet(1);
        }
        return value;
    }

    protected Object buildValue(Object value) {
        return new SoftReference(value);
    }

    @Override
    public void put(Object key, Object value) {
        if (key == null || value == null) return;
        putTimes.addAndGet(1);
        super.put(key, buildValue(value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (key == null || value == null) return null;
        putTimes.addAndGet(1);
        return super.putIfAbsent(key, buildValue(value));
    }

    @Override
    public long getPutTimes() {
        return putTimes.get();
    }

}
