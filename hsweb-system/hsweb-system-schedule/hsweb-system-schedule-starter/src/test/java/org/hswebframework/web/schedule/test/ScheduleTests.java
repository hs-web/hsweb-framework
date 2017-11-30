package org.hswebframework.web.schedule.test;

import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@EnableScheduling
@Configuration
public class ScheduleTests extends SimpleWebApplicationTests {

    AtomicInteger counter = new AtomicInteger();

    CountDownLatch countDownLatch = new CountDownLatch(1);

    @Scheduled(cron = "0/1 * * * * ?")
    public void quartzTest() {
        counter.incrementAndGet();
        countDownLatch.countDown();
    }

    @Test
    public void test() throws InterruptedException {
        countDownLatch.await(100, TimeUnit.SECONDS);
        Assert.assertTrue(counter.get() > 0);
    }
}
