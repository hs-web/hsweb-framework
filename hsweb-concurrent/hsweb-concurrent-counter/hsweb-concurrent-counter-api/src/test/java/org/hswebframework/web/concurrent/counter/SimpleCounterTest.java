package org.hswebframework.web.concurrent.counter;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleCounterTest {

    private CounterManager counterManager = new SimpleCounterManager();

    @Test
    public void testSimple() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> counterManager.getCounter("test").increment()).start();
        }

        Thread.sleep(500);
        Assert.assertEquals(counterManager.getCounter("test").get(), 100);
    }

}