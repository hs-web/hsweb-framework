package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThreadLocalUtilsTests {

    @Test
    public void testAll() {
        ThreadLocalUtils.put("test", "1");

        Assert.assertEquals(ThreadLocalUtils.get("test"), "1");

        ThreadLocalUtils.get("test2", () -> "2");

        Assert.assertEquals(ThreadLocalUtils.get("test2"), "2");

        Assert.assertEquals(ThreadLocalUtils.getAndRemove("test2"), "2");

        Assert.assertTrue(ThreadLocalUtils.get("test2") == null);

        ThreadLocalUtils.remove("test");

        Assert.assertTrue(ThreadLocalUtils.get("test") == null);

        ThreadLocalUtils.put("test", "1");
        ThreadLocalUtils.put("test2", "2");

        Assert.assertTrue(ThreadLocalUtils.getAll().size()==2);
        ThreadLocalUtils.clear();

        Assert.assertTrue(ThreadLocalUtils.getAll().size()==0);
    }
}