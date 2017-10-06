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

import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DynamicScheduleTests extends SimpleWebApplicationTests {

    @Autowired
    private ScheduleJobService scheduleJobService;


    public static final AtomicLong counter=new AtomicLong();
    private String id;

    @Before
    public void initJob() throws InterruptedException {
        Thread.sleep(5000);
        id = scheduleJobService.insert(createJob());
        scheduleJobService.enable(id);
    }

    public ScheduleJobEntity createJob() {
        ScheduleJobEntity entity = scheduleJobService.createEntity();
        entity.setStatus(DataStatus.STATUS_ENABLED);
        entity.setType("test");
        entity.setLanguage("javascript");
        entity.setScript("" +
                "org.hswebframework.web.schedule.test.DynamicScheduleTests.counter.incrementAndGet()\n" +
                "java.lang.System.out.println('job running...')");
        entity.setQuartzConfig("{\"type\":\"cron\",\"config\":\"0/1 * * * * ?\"}");
        return entity;
    }

    @Test
    public void testCreateJob() throws InterruptedException {
        Thread.sleep(20000);
        Assert.assertTrue(counter.get()>0);
    }
}
