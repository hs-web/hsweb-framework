package org.hswebframework.web.controller.schedule;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebframework.web.service.schedule.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  调度任务
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.scheduleJob:scheduleJob}")
@Authorize(permission = "scheduleJob")
@AccessLogger("调度任务")
public class ScheduleJobController implements SimpleGenericEntityController<ScheduleJobEntity, String, QueryParamEntity> {

    private ScheduleJobService scheduleJobService;
  
    @Autowired
    public void setScheduleJobService(ScheduleJobService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }
  
    @Override
    public ScheduleJobService getService() {
        return scheduleJobService;
    }
}
