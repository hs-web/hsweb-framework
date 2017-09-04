package org.hswebframework.web.workflow.flowable.controller;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author wangwei
 * @Date 2017/9/4.
 */
@RestController
@RequestMapping("workflow/utils/")
public class FlowableUtilsController {

    @Autowired
    BpmActivityService bpmActivityService;

    @GetMapping("{procDefId}/acts")
    public ResponseMessage<List<ActivityImpl>> acts(@PathVariable String procDefId){
        List<ActivityImpl> activities = bpmActivityService.getActivitiesById(procDefId,null);
        return ResponseMessage.ok(activities);
    }
}
