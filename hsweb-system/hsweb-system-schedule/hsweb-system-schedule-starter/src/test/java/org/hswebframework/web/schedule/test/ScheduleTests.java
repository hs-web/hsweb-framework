package org.hswebframework.web.schedule.test;

import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Scheduled(cron = "0/1 * * * * ?")
    public void quartzTest() {
        System.out.println(1234);
        counter.incrementAndGet();
    }

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(10000);
        Assert.assertTrue(counter.get() > 0);
        System.out.println(counter.get());
    }
}
