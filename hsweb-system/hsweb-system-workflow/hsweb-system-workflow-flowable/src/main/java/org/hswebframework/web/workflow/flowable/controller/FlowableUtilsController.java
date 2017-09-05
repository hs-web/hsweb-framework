package org.hswebframework.web.workflow.flowable.controller;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.organizational.RelationDefineEntity;
import org.hswebframework.web.entity.organizational.SimpleRelationDefineEntity;
import org.hswebframework.web.service.organizational.RelationDefineService;
import org.hswebframework.web.service.organizational.RelationInfoService;
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
    RelationDefineService relationDefineService;
    @Autowired
    RelationInfoService   relationInfoService;

    @GetMapping("{procDefId}/acts")
    public ResponseMessage<Map<String, Object>> acts(@PathVariable String procDefId) {
        Map<String, Object> map = new HashMap<>();
        List<ActivityImpl> activities = bpmActivityService.getActivitiesById(procDefId, null);
        for (ActivityImpl activity : activities) {
            if (activity.getProperty("type").equals("userTask")) map.put(activity.getId(), activity.getProperty("name"));
        }
        return ResponseMessage.ok(map);
    }
// see hsweb-system-organizational-controller
//    @GetMapping("relation-define")
//    public ResponseMessage<List<RelationDefineEntity>> getRelationDefines() {
//        List<RelationDefineEntity> list = relationDefineService
//                .select(single(RelationDefineEntity.status, DataStatus.STATUS_ENABLED));
//
//        return ResponseMessage.ok(list);
//    }

//    @PostMapping("act/{actId}-{defineId}")
//    public ResponseMessage
}
