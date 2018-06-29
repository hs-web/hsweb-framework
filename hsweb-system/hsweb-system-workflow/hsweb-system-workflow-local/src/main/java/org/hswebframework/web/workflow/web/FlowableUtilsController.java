package org.hswebframework.web.workflow.web;

import io.swagger.annotations.Api;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wangwei
 * @Date 2017/9/4.
 */
@RestController
@RequestMapping("workflow/utils")
@Api(tags = "工作流-节点配置", description = "工作流节点配置")
@Authorize(permission = "workflow-utils", description = "节点配置")
public class FlowableUtilsController {

    @Autowired
    BpmActivityService bpmActivityService;


    /**
     * 获取流程所有配置节点
     *
     * @param procDefId
     * @return
     */
    @GetMapping("{procDefId}/acts")
    public ResponseMessage<List<Map<String, Object>>> acts(@PathVariable String procDefId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<ActivityImpl> activities = bpmActivityService.getActivitiesById(procDefId, null);
        for (ActivityImpl activity : activities) {
            Map<String, Object> map = new HashMap<>();
            if ("startEvent".equals(activity.getProperty("type"))) {
                map.put("id", activity.getId());
                map.put("name", "流程发起者");
//                map.put("info", actDefService.selectSingle(single(ActDefEntity.actId, activity.getId())));
            } else if ("userTask".equals(activity.getProperty("type"))) {
                map.put("id", activity.getId());
                map.put("name", activity.getProperty("name").toString());
//                map.put("info", actDefService.selectSingle(single(ActDefEntity.actId, activity.getId())));
            }
            if (map.size() > 0) {
                list.add(map);
            }
        }
        return ResponseMessage.ok(list);
    }

    /**
     * 给流程节点配置表单与人员矩阵
     *
     * @param actDefEntity
     * @return
     */
//    @PostMapping("act")
//    public ResponseMessage<String> setActClaimDef(@RequestBody ActDefEntity actDefEntity) {
//        return ResponseMessage.ok(actDefService.saveOrUpdate(actDefEntity));
//    }

}
