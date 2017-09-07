package org.hswebframework.web.workflow.flowable.controller;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.service.BpmProcessService;
import org.hswebframework.web.workflow.flowable.service.BpmTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.single;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
@RestController
@RequestMapping("/workflow/proc-def/")
public class FlowableCoreController {
    @Autowired
    BpmTaskService bpmTaskService;
    @Autowired
    BpmProcessService bpmProcessService;
    @Autowired
    BpmActivityService bpmActivityService;
    @Autowired
    ActDefService actDefService;
    @Autowired
    DynamicFormOperationService dynamicFormOperationService;

    /**
     * 获取所有可用流程（流程配置与流程启动都可用该方法获取）
     * @return
     */
    @GetMapping("index")
    public ResponseMessage<Map<String, Object>> index(){
        List<ProcessDefinition> list = bpmProcessService.getAllProcessDefinition();
        Map<String, Object> map = new HashMap<>();
        for(ProcessDefinition processDefinition : list){
            map.put(processDefinition.getName(),processDefinition.getId());
        }
        return ResponseMessage.ok(map);
    }

    /**
     * 进入流程表单
     * @param procDefId
     * @return
     */
    @GetMapping("open-form/{id}")
    public ResponseMessage<Map<String,PagerResult<Object>>> openForm(@PathVariable("id") String procDefId){
        Map<String,PagerResult<Object>> map = new HashMap<>();
        ActivityImpl activity = bpmActivityService.getStartEvent(procDefId);
        ActDefEntity actDefEntity = actDefService.selectSingle(single(ActDefEntity.actId,activity.getId()));
        map.put(activity.getProcessDefinition().getKey(),
                dynamicFormOperationService.selectPager(actDefEntity.getFormId(), null));
        return ResponseMessage.ok(map);
    }

    /**
     * 保存表单，启动流程
     * @param formId
     * @param defKey
     * @param data
     * @return
     */
    @PostMapping("start/{formId}-{defKey}")
    public ResponseMessage<Map<String, Object>> startProc(@PathVariable String formId,@PathVariable String defKey, @RequestBody Map<String, Object> data) {
        dynamicFormOperationService.insert(formId, data);
        bpmProcessService.startProcessInstance("admin",defKey,null,null,formId,null);
        return ResponseMessage.ok(data);
    }
}
