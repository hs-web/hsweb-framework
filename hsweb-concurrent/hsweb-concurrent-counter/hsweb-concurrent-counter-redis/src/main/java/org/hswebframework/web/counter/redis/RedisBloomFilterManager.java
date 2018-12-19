package org.hswebframework.web.counter.redis;

import lombok.AllArgsConstructor;
import org.hswebframework.web.concurrent.counter.AbstractBoomFilterManager;
import org.hswebframework.web.concurrent.counter.BloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

@AllArgsConstructor
public class RedisBloomFilterManager extends AbstractBoomFilterManager {

    private RedissonClient redissonClient;

    @Override
    protected BloomFilter createBloomFilter(String name) {
        RBloomFilter<String> filter = redissonClient.getBloomFilter("hsweb:bloom-filter:" + name, StringCodec.INSTANCE);
        filter.tryInit(55000000L, 0.01);
        return new BloomFilter() {
            @Override
            public boolean put(String unique) {
                return filter.add(unique);
            }

            @Override
            public boolean contains(String unique) {
                return filter.contains(unique);
            }
        };
    }
}
