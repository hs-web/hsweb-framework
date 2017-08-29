package org.hswebframework.web.service.schedule;

import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 调度任务 服务类
 *
 * @author hsweb-generator-online
 */
public interface ScheduleJobService extends CrudService<ScheduleJobEntity, String> {

    void enable(String id);

    void disable(String id);
}
