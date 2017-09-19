package org.hswebframework.web.workflow.flowable.controller;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.single;

/**
 * @Author wangwei
 * @Date 2017/9/4.
 */
@RestController
@RequestMapping("workflow/utils/")
public class FlowableUtilsController {

    @Autowired
    BpmActivityService bpmActivityService;

    @Autowired
    ActDefService actDefService;

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
            if (activity.getProperty("type").equals("startEvent")) {
                map.put("id", activity.getId());
                map.put("name", "流程发起者");
                map.put("info", actDefService.selectSingle(single(ActDefEntity.actId, activity.getId())));
            } else if (activity.getProperty("type").equals("userTask")) {
                map.put("id", activity.getId());
                map.put("name", activity.getProperty("name").toString());
                map.put("info", actDefService.selectSingle(single(ActDefEntity.actId, activity.getId())));
            }
            if (map.size() > 0) list.add(map);
        }
        return ResponseMessage.ok(list);
    }

    /**
     * 给流程节点配置表单与人员矩阵
     *
     * @param actDefEntity
     * @return
     */
    @PostMapping("act")
    public ResponseMessage<String> setActClaimDef(@RequestBody ActDefEntity actDefEntity) {
        return ResponseMessage.ok(actDefService.saveOrUpdate(actDefEntity));
    }

}
