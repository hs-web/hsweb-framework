package org.hswebframework.web.service.schedule;

import java.util.Map;

/**
 * @author zhouhao
 */
public interface ScheduleJobExecutor {
    Object doExecuteJob(String jobId, Map<String, Object> parameter);
}
