package org.hswebframework.web.concurrent.counter;

public interface BloomFilterManager {

    BloomFilter getBloomFilter(String key);
}
