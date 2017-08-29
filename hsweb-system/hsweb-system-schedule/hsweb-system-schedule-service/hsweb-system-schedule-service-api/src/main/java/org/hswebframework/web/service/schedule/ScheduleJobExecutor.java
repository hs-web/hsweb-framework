package org.hswebframework.web.service.schedule;

import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ScheduleJobExecutor {
    Object doExecuteJob(String jobId, Map<String, Object> parameter);
}
