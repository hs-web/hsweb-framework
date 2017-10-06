package org.hswebframework.web.schedule.configuration;

import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.service.schedule.simple.DynamicJobFactory;
import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.quartz.SchedulerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@EnableConfigurationProperties(SchedulerProperties.class)
@ConditionalOnMissingBean({Scheduler.class, SchedulerFactoryBean.class})
public class ScheduleAutoConfiguration {
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
        return new DynamicJobFactory(new AdaptableJobFactory());
    }

    @Bean
    public AutoCreateTable autoCreateTable() {
        return new AutoCreateTable();
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
