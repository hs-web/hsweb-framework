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

        Assert.assertTrue(filter.put("test"));
        Assert.assertFalse(filter.put("test"));

    }

}