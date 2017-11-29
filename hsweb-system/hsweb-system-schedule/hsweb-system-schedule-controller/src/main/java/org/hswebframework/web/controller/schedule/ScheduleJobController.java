package org.hswebframework.web.controller.schedule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.schedule.ScheduleJobExecutor;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 调度任务
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.scheduleJob:schedule/job}")
@Authorize(permission = "schedule-job", description = "定时调度管理")
@Api(value = "定时调度管理",tags = "定时调度管理")
public class ScheduleJobController implements SimpleGenericEntityController<ScheduleJobEntity, String, QueryParamEntity> {

    private ScheduleJobService scheduleJobService;

    private ScheduleJobExecutor scheduleJobExecutor;

    @Autowired
    @Authorize(ignore = true)
    public void setScheduleJobExecutor(ScheduleJobExecutor scheduleJobExecutor) {
        this.scheduleJobExecutor = scheduleJobExecutor;
    }

    @Autowired
    @Authorize(ignore = true)
    public void setScheduleJobService(ScheduleJobService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }

    @Override
    public ScheduleJobService getService() {
        return scheduleJobService;
    }

    @PutMapping("/{id}/enable")
    @Authorize(action = Permission.ACTION_ENABLE)
    @ApiOperation("启用任务")
    public ResponseMessage<Void> enable(@PathVariable String id) {
        scheduleJobService.enable(id);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/disable")
    @Authorize(action = Permission.ACTION_DISABLE)
    @ApiOperation("禁用任务")
    public ResponseMessage<Void> disable(@PathVariable String id) {
        scheduleJobService.disable(id);
        return ResponseMessage.ok();
    }

    @PostMapping("/{id}/execute")
    @Authorize(action = "execute", description = "执行任务")
    @ApiOperation("执行任务")
    public ResponseMessage<Object> execute(@PathVariable String id, @RequestBody Map<String, Object> args) {
        return ResponseMessage.ok(scheduleJobExecutor.doExecuteJob(id, args));
    }
}
