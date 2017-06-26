package org.hswebframework.web.concurrent.lock.redis;


import org.hswebframework.web.concurrent.lock.LockManager;
import org.junit.Assert;
import org.redisson.Redisson;
import org.redisson.config.Config;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RedissonLockTest {
    static long counter = 0;

    static LockManager lockManager = null;

    static Redisson redisson;

    public static LockManager createLockFactory() {
        if (lockManager != null) {
            return lockManager;
        }
        Config config = new Config();
//        config.setUseLinuxNativeEpoll(true);
        config.useSingleServer().setAddress("127.0.0.1:6379");
        redisson = (Redisson) Redisson.create(config);
        return lockManager = new RedissonLockManager(redisson);
    }

    public static void main(String[] args) throws InterruptedException {
        testLock();

        testReadWriteLock();
        redisson.shutdown();
    }

    public static void testReadWriteLock() throws InterruptedException {
        counter = 0;
        LockManager lockManager = createLockFactory();

        ReadWriteLock readWriteLock = lockManager.getReadWriteLock("foo");

        Lock readLock = readWriteLock.readLock();

        Lock writeLock = readWriteLock.writeLock();
        Consumer[] consumer = new Consumer[1];
        consumer[0] = System.out::println;
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int i1 = 0; i1 < 10; i1++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    writeLock.lock();
                    long tmp = ++counter;

                    //判断增加的值与 读取的值一致
                    consumer[0] = l -> Assert.assertEquals(Long.valueOf(tmp), l);
                    System.out.println("write:" + counter);
                    writeLock.unlock();
                }
            }).start();
            new Thread(() -> {
                for (int i1 = 0; i1 < 10; i1++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    readLock.lock();
                    consumer[0].accept(counter);
                    System.out.println("read:" + counter);
                    readLock.unlock();
                }
            }).start();
        }
        Thread.sleep(5000);
        System.out.println("wait 5s");
    }

    public static void testLock() throws InterruptedException {
        counter = 0;
        LockManager lockManager = createLockFactory();
        Lock lock = lockManager.getLock("foo");
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                lock.lock();
                for (int i1 = 0; i1 < 100; i1++) {
                    counter++;
                }
                lock.unlock();
            }).start();
        }
        Thread.sleep(1000);
        System.out.println(counter);
        Assert.assertEquals(counter, 100 * 100);
    }
}