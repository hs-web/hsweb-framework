package org.hswebframework.web.workflow.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.hswebframework.web.workflow.service.BpmProcessService;
import org.hswebframework.web.workflow.service.BpmTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
@RestController
@RequestMapping("/workflow/proc-def/")
@Api(tags = "工作流-流程管理",description = "工作流流程管理")
@Authorize(permission = "workflow-process",description = "工作流流程管理")
public class FlowableCoreController {
    @Autowired
    BpmTaskService bpmTaskService;
    @Autowired
    BpmProcessService bpmProcessService;
    @Autowired
    BpmActivityService bpmActivityService;
    @Autowired(required = false)
    DynamicFormOperationService dynamicFormOperationService;

    private void assertDynamicFormReady(){
        Assert.notNull(dynamicFormOperationService,"未引入动态表单依赖");
    }
    /**
     * 获取所有可用流程（流程配置与流程启动都可用该方法获取）
     * @return key为流程定义的id。value为name
     */
    @GetMapping("/available")
    @ApiOperation("获取所有可用流程定义信息")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<Map<String, String>> getALlAvailableProcessDefinition(){
        List<ProcessDefinition> list = bpmProcessService.getAllProcessDefinition();
        return ResponseMessage.ok(list
                .stream()
                .collect(Collectors.toMap(ProcessDefinition::getId,ProcessDefinition::getName)));
    }

    /**
     * 查询流程表单数据
     * @param procDefId 流程定义id
     */
    @GetMapping("form/{id}")
    @ApiOperation("查询流程的表单数据")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<PagerResult<Object>> openForm(@PathVariable("id") String procDefId, QueryParamEntity param){
        assertDynamicFormReady();
        Map<String,PagerResult<Object>> map = new HashMap<>();
        ActivityImpl activity = bpmActivityService.getStartEvent(procDefId);

//        ActDefEntity actDefEntity = actDefService.selectSingle(single(ActDefEntity.actId,activity.getId()));
//        if(actDefEntity!=null){
//            return ResponseMessage.ok(dynamicFormOperationService.selectPager(actDefEntity.getFormId(),param));
//        }
        throw new NotFoundException("表单不存在");
    }

    /**
     * 提交表单数据并启动流程
     */
    @PostMapping("start/{formId}-{defId}")
    @ApiOperation("提交表单数据并启动流程")
    public ResponseMessage<Map<String, Object>> startProc(@PathVariable String formId,@PathVariable String defId, @RequestBody Map<String, Object> data) {
        assertDynamicFormReady();
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        dynamicFormOperationService.insert(formId, data);
        ProcessDefinition processDefinition = bpmProcessService.getProcessDefinitionById(defId);
//        bpmProcessService.startProcessInstance(authorization.getPersonnel().getId(),processDefinition.getKey(),null,null,formId,null);
        return ResponseMessage.ok(data);
    }

    /**
     * 获取待办任务
     * @return
     */
    @GetMapping("tasks")
    @ApiOperation("获取代办任务")
    public ResponseMessage<List<Task>> getMyTasks() {
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        String userId = authorization.getPersonnel().getId();
        List<Task> tasks = bpmTaskService.claimList(userId);
        return ResponseMessage.ok(tasks).include(Task.class, "id", "name", "createTime", "executionId"
                , "parentTaskId", "processInstanceId", "processDefinitionId", "taskDefinitionKey")
                .exclude(Task.class, "definition", "mainFormData");
    }

    /**
     * 办理任务
     * @param formId
     * @param taskId
     * @return
     */
    @PutMapping("complete/{formId}-{taskId}")
    public ResponseMessage<Map<String,Object>> complete(@PathVariable String formId,@PathVariable String taskId){
        assertDynamicFormReady();
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        String userId = authorization.getPersonnel().getId();
//        dynamicFormOperationService.update(formId,null);
        // 认领
        bpmTaskService.claim(taskId,userId);
        // 办理
        bpmTaskService.complete(taskId, userId, null, null);
        return ResponseMessage.ok();
    }
}
