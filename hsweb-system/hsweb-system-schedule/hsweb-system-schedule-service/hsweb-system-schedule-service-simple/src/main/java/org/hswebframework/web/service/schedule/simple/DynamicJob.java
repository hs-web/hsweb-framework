package org.hswebframework.web.service.schedule.simple;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@DisallowConcurrentExecution
public class DynamicJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
