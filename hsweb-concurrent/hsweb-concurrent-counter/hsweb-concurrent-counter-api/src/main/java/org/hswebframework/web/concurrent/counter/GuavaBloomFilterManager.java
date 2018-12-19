package org.hswebframework.web.concurrent.counter;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class GuavaBloomFilterManager extends AbstractBoomFilterManager {

    private long expectedInsertions = 55000000L;

    private double fpp = 0.01;

    @Override
    protected BloomFilter createBloomFilter(String name) {
        com.google.common.hash.BloomFilter<String>
                filter = com.google.common.hash.BloomFilter.create((str, sink) -> sink.putString(str, StandardCharsets.UTF_8), expectedInsertions, fpp);
        return new BloomFilter() {
            @Override
            public boolean put(String unique) {
                return filter.put(unique);
            }

            @Override
            public boolean contains(String unique) {
                return filter.mightContain(unique);
            }
        };
    }
}
