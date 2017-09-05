package org.hswebframework.web.workflow.flowable.controller;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wangwei
 * @Date 2017/9/4.
 */
@RestController
@RequestMapping("workflow/utils/")
public class FlowableUtilsController {

    @Autowired
    BpmActivityService    bpmActivityService;

    @Autowired
    ActDefService actDefService;

    @GetMapping("{procDefId}/acts")
    public ResponseMessage<Map<String, Object>> acts(@PathVariable String procDefId) {
        Map<String, Object> map = new HashMap<>();
        List<ActivityImpl> activities = bpmActivityService.getActivitiesById(procDefId, null);
        for (ActivityImpl activity : activities) {
            if (activity.getProperty("type").equals("userTask")) map.put(activity.getId(), activity.getProperty("name"));
        }
        return ResponseMessage.ok(map);
    }

    @PostMapping("act/{actId}-{defId}")
    public ResponseMessage<Map<String, Object>> setActClaimDef(@PathVariable String actId, @PathVariable String defId){
        Map<String, Object> map = new HashMap<>();
        ActDefEntity actDefEntity = actDefService.createEntity();
        actDefEntity.setActId(actId);
        actDefEntity.setDefId(defId);
        actDefService.insert(actDefEntity);
        return ResponseMessage.ok(map);
    }
}
