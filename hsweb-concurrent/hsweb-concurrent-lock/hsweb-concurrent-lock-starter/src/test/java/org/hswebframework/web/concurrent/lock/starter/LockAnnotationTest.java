package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class LockAnnotationTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    private LockService lockService;

    @Autowired
    private LockManager lockManager;

    @Test
    public void testLock() throws InterruptedException {
        new Thread(() -> {
            System.out.println("锁住");
            lockService.testLockSleep("test", 2000);
            System.out.println("解锁");
        }).start();
        Thread.sleep(200);
        System.out.println("开始任务1");
        lockService.testLock("test");
        System.out.println("任务1结束");
        for (int i = 0; i < 100; i++) {
            new Thread(() -> lockService.testLock("test")).start();
        }
        Thread.sleep(5000);
        Assert.assertEquals(lockService.getCounter(), 101);

        lockService.reset();
    }


    @Test
    public void testReadLock() throws InterruptedException {
        new Thread(() -> {
            try {
                System.out.println("锁住");
                lockManager.getReadWriteLock("lock_test").writeLock().lock();
                Thread.sleep(2000); //停顿2秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("解锁");
            lockManager.getReadWriteLock("lock_test").writeLock().unlock();
        }).start();
        Thread.sleep(200);
        System.out.println("开始任务1");
        lockService.testReadLock("test");
        System.out.println("任务1结束");
        for (int i = 0; i < 100; i++) {
            new Thread(() -> lockService.testWriteLock("test")).start();
        }
        Thread.sleep(5000);
        Assert.assertEquals(lockService.getCounter(), 101);
        lockService.reset();
    }


}