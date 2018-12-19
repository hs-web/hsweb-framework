package org.hswebframework.web.concurrent.counter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 */
public abstract class AbstractBoomFilterManager implements BloomFilterManager {

    private final Map<String, BloomFilter> counterStore = new HashMap<>(128);

    @Override
    public BloomFilter getBloomFilter(String key) {
        BloomFilter filter = counterStore.get(key);
        if (filter != null) {
            return filter;
        }
        synchronized (counterStore) {
            return counterStore.computeIfAbsent(key, this::createBloomFilter);
        }
    }

    protected abstract BloomFilter createBloomFilter(String name);
}
