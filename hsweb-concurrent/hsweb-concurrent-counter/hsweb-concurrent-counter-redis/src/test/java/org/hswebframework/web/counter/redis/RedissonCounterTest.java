package org.hswebframework.web.counter.redis;

import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.junit.Assert;
import org.redisson.Redisson;
import org.redisson.config.Config;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RedissonCounterTest {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
//        config.setUseLinuxNativeEpoll(true);
        config.useSingleServer().setAddress("127.0.0.1:6379");
        Redisson redisson = (Redisson) Redisson.create(config);

        CounterManager counterManager = new RedissonCounterManager(redisson);
        //重置
        counterManager.getCounter("test").set(0);

        for (int i = 0; i < 100; i++) {
            new Thread(() -> counterManager.getCounter("test").increment()).start();
        }

        Thread.sleep(500);
        Assert.assertEquals(counterManager.getCounter("test").get(), 100);
        redisson.shutdown();

    }
}