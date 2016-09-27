/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.service.impl.quartz;

import org.hsweb.web.service.impl.AbstractTestCase;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.MutableTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

public class QuartzTests extends AbstractTestCase {
    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private DataSource dataSource;

    @Resource
    private PlatformTransactionManager platformTransactionManager;

    @Test
    public void testQuartz() throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setApplicationContext(applicationContext);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setTransactionManager(platformTransactionManager);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setSchedulerFactoryClass(StdSchedulerFactory.class);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.setBeanName("schedulerFactory");
        Scheduler scheduler = schedulerFactoryBean.getObject();

//        MutableTrigger trigger   = CronScheduleBuilder.cronSchedule("0/2 * * * * ?").build();
//        scheduler.deleteJob(new JobKey("test"));
//        trigger.setKey(new TriggerKey("test"));
//        scheduler.scheduleJob(JobBuilder.newJob(TestJob.class).withIdentity("test").build(), trigger);
//        scheduler.deleteJob(new JobKey("test"));
//        scheduler.scheduleJob(JobBuilder.newJob(TestJob.class).withIdentity("test").build(), trigger);
        scheduler.resumeAll();
        scheduler.start();

//        schedulerFactoryBean.start();
        Thread.sleep(60 * 1000);
    }


}
