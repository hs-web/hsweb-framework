package org.hswebframework.web.service.schedule.simple;

import org.hswebframework.web.service.schedule.ScheduleJobExecutor;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DynamicJobFactory implements JobFactory {

    public static final String JOB_ID_KEY = "dynamic-job-id:";

    private JobFactory defaultFactory;

    private ScheduleJobExecutor scheduleJobExecutor;

    public DynamicJobFactory(JobFactory defaultFactory) {
        this.defaultFactory = defaultFactory;
    }

    @Autowired
    public void setScheduleJobExecutor(ScheduleJobExecutor scheduleJobExecutor) {
        this.scheduleJobExecutor = scheduleJobExecutor;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Map<String, Object> data = bundle.getJobDetail().getJobDataMap();
        String jobId = (String) data.get(JOB_ID_KEY);
        if (null == jobId || bundle.getJobDetail().getJobClass() != DynamicJob.class) return defaultFactory.newJob(bundle, scheduler);
        return  context -> scheduleJobExecutor.doExecuteJob(jobId, data);
    }
}
