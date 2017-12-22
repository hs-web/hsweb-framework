package org.hswebframework.web.schedule.test;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DynamicScheduleTests extends SimpleWebApplicationTests {

    @Autowired
    private ScheduleJobService scheduleJobService;


    public static final CountDownLatch counter=new CountDownLatch(1);
    public static final AtomicLong value=new AtomicLong();
    private String id;

    @Before
    public void initJob() throws InterruptedException {
        id = scheduleJobService.insert(createJob());
        scheduleJobService.enable(id);
    }

    public ScheduleJobEntity createJob() {
        ScheduleJobEntity entity = scheduleJobService.createEntity();
        entity.setStatus(DataStatus.STATUS_ENABLED);
        entity.setType("test");
        entity.setLanguage("javascript");
        entity.setTags("core2");
        entity.setScript("" +
                "org.hswebframework.web.schedule.test.DynamicScheduleTests.value.incrementAndGet();\n" +
                "org.hswebframework.web.schedule.test.DynamicScheduleTests.counter.countDown();\n" +
                "java.lang.System.out.println('script job running...')");
        entity.setQuartzConfig("{\"type\":\"cron\",\"config\":\"0/1 * * * * ?\"}");
        return entity;
    }

    @Test
    public void testCreateJob() throws InterruptedException {
        counter.await(100, TimeUnit.SECONDS);
        Assert.assertTrue(value.get()>0);
    }
}
