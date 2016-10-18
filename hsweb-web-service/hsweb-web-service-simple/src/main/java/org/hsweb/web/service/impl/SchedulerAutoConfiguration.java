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

package org.hsweb.web.service.impl;

import org.hsweb.web.service.impl.quartz.SimpleJobFactory;
import org.quartz.Calendar;
import org.quartz.SchedulerListener;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@ConditionalOnClass(QuartzScheduler.class)
@EnableConfigurationProperties(SchedulerProperties.class)
@AutoConfigureAfter(DataBaseAutoConfiguration.class)
public class SchedulerAutoConfiguration {

    @Autowired
    private SchedulerProperties schedulerProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired(required = false)
    private Map<String, Calendar> calendarMap;

    @Autowired(required = false)
    private SchedulerListener[] schedulerListeners;

    @Bean
    public JobFactory jobFactory() {
        SimpleJobFactory jobFactory = new SimpleJobFactory();
        jobFactory.setDefaultFactory(new AdaptableJobFactory());
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(JobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setApplicationContext(applicationContext);
        schedulerFactoryBean.setAutoStartup(schedulerProperties.isAutoStartup());
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setTransactionManager(platformTransactionManager);
        schedulerFactoryBean.setOverwriteExistingJobs(schedulerProperties.isOverwriteExistingJobs());
        schedulerFactoryBean.setSchedulerFactoryClass(StdSchedulerFactory.class);
        schedulerFactoryBean.setBeanName(schedulerProperties.getBeanName());
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(schedulerProperties.isWaitOnShutdown());
        schedulerFactoryBean.setQuartzProperties(schedulerProperties.getProperties());
        schedulerFactoryBean.setStartupDelay(schedulerProperties.getStartupDelay());
        schedulerFactoryBean.setCalendars(calendarMap);
        schedulerFactoryBean.setSchedulerListeners(schedulerListeners);
        return schedulerFactoryBean;
    }
}
