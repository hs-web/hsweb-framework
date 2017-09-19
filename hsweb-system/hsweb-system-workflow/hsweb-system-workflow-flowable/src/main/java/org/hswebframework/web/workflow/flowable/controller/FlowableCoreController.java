package org.hswebframework.web.workflow.flowable.controller;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
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
     * @param defId
     * @param data
     * @return
     */
    @PostMapping("start/{formId}-{defId}")
    public ResponseMessage<Map<String, Object>> startProc(@PathVariable String formId,@PathVariable String defId, @RequestBody Map<String, Object> data) {
        PersonnelAuthorization authorization = PersonnelAuthorization
                .current()
                .orElseThrow(NotFoundException::new);
        dynamicFormOperationService.insert(formId, data);
        ProcessDefinition processDefinition = bpmProcessService.getProcessDefinitionById(defId);
        bpmProcessService.startProcessInstance(authorization.getPersonnel().getId(),processDefinition.getKey(),null,null,formId,null);
        return ResponseMessage.ok(data);
    }

    /**
     * 获取待办任务
     * @return
     */
    @GetMapping("tasks")
    public ResponseMessage<List<Task>> getMyTasks() {
        PersonnelAuthorization authorization = PersonnelAuthorization
                .current()
                .orElseThrow(NotFoundException::new);
        String userId = authorization.getPersonnel().getId();
        List<Task> tasks = bpmTaskService.claimList(userId);
        return ResponseMessage.ok(tasks).include(Task.class, "id", "name", "createTime", "executionId"
                , "parentTaskId", "processInstanceId", "processDefinitionId", "taskDefinitionKey")
                .exclude(Task.class, "definition", "mainFormData");
    }

    /**
     * 办理
     * @param formId
     * @param taskId
     * @param paramEntity
     * @return
     */
    @PutMapping("complete/{formId}-{taskId}")
    public ResponseMessage<Map<String,Object>> complete(@PathVariable String formId,@PathVariable String taskId, @RequestBody UpdateParamEntity<Map<String, Object>> paramEntity){
        PersonnelAuthorization authorization = PersonnelAuthorization
                .current()
                .orElseThrow(NotFoundException::new);
        String userId = authorization.getPersonnel().getId();
        dynamicFormOperationService.update(formId,paramEntity);
        // 认领
        bpmTaskService.claim(taskId,userId);
        // 办理
        bpmTaskService.complete(taskId, userId, null, null);
        return ResponseMessage.ok();
    }
}
