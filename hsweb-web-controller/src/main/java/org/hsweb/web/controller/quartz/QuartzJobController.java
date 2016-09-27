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

package org.hsweb.web.controller.quartz;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.quartz.QuartzJob;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.springframework.web.bind.annotation.*;
import org.hsweb.web.service.quartz.QuartzJobService;

import javax.annotation.Resource;

/**
 * 定时调度任务控制器
 * Created by hsweb-generator
 */
@RestController
@RequestMapping(value = "/quartz")
@AccessLogger("定时调度任务")
@Authorize(module = "quartz")
public class QuartzJobController extends GenericController<QuartzJob, String> {

    @Resource
    private QuartzJobService quartzJobService;

    @Resource
    private QuartzJobHistoryService quartzJobHistoryService;

    @Override
    public QuartzJobService getService() {
        return this.quartzJobService;
    }


    @RequestMapping(value = "/{id}/enable", method = RequestMethod.PUT)
    @AccessLogger("启用任务")
    @Authorize(action = "enable")
    public ResponseMessage enable(@PathVariable("id") String id) {
        quartzJobService.enable(id);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/{id}/disable", method = RequestMethod.PUT)
    @AccessLogger("禁用任务")
    @Authorize(action = "disable")
    public ResponseMessage disable(@PathVariable("id") String id) {
        quartzJobService.disable(id);
        return ResponseMessage.ok();
    }


    @RequestMapping(value = "/history/{jobId}", method = RequestMethod.GET)
    @AccessLogger("执行历史")
    @Authorize(action = "history")
    public ResponseMessage history(@PathVariable("jobId") String jobId, QueryParam param) {
        param.where("jobId", jobId);
        return ResponseMessage.ok(quartzJobHistoryService.selectPager(param)).onlyData();
    }

    @RequestMapping(value = "/cron/exec-times/{number:\\d+}", method = RequestMethod.GET, params = "cron")
    @AccessLogger("获取最近几次执行时间")
    @Authorize
    public ResponseMessage history(@RequestParam("cron") String cron, @PathVariable("number") int number) {
        return ResponseMessage.ok(quartzJobService.getExecTimes(cron, number));
    }
}
