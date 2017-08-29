package org.hswebframework.web.schedule.test;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DynamicScheduleTests extends SimpleWebApplicationTests {

    @Autowired
    private ScheduleJobService scheduleJobService;


    public ScheduleJobEntity createJob() {
        ScheduleJobEntity entity = scheduleJobService.createEntity();
        entity.setStatus(DataStatus.STATUS_ENABLED);
        entity.setType("test");
        entity.setLanguage("javascript");
        entity.setScript("java.lang.System.out.println('job running...')");
        entity.setQuartzConfig("{\"type\":\"cron\",\"config\":\"0/5 * * * * ?\"}");
        return entity;
    }

    @Test
    public void testCreateJob() throws InterruptedException {
        Thread.sleep(5000);
        String id = scheduleJobService.insert(createJob());
        scheduleJobService.enable(id);

        Thread.sleep(40000);
    }
}
