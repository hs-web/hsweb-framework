package org.hswebframework.web.concurrent.counter;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class GuavaBloomFilterManagerTest {

    @Test
    public void testGuavaBloomFilter() {
        BloomFilterManager manager = new GuavaBloomFilterManager();

        BloomFilter filter = manager.getBloomFilter("test");
        Assert.assertNotNull(filter);

        for (int i = 0; i < 100000; i++) {
            Assert.assertTrue(filter.put("test" + i));
            Assert.assertTrue(filter.contains("test" + i));
            Assert.assertFalse(filter.put("test" + i));
        }

    }

}