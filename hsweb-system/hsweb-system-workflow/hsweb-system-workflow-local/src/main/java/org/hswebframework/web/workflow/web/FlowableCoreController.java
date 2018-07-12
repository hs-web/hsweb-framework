package org.hswebframework.web.workflow.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.workflow.service.ActivityConfigurationService;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.hswebframework.web.workflow.service.BpmProcessService;
import org.hswebframework.web.workflow.service.BpmTaskService;
import org.hswebframework.web.workflow.service.request.CompleteTaskRequest;
import org.hswebframework.web.workflow.service.request.StartProcessRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @date 2017/9/5.
 */
@RestController
@RequestMapping("/workflow/process/")
@Api(tags = "工作流-流程管理", description = "工作流流程管理")
@Authorize(permission = "workflow-process", description = "工作流流程管理")
public class FlowableCoreController {

    @Autowired
    private BpmTaskService bpmTaskService;

    @Autowired
    private BpmProcessService bpmProcessService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ActivityConfigurationService activityConfigurationService;

    @PostMapping("start/key/{defineKey}")
    @ApiOperation("提交表单数据并根据流程定义key启动流程")
    @Authorize(merge = false)
    public ResponseMessage<String> startProcessByKey(@PathVariable String defineKey,
                                                     @RequestBody Map<String, Object> data,
                                                     Authentication authentication) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(defineKey)
                .active()
                .singleResult();

        if (null == definition) {
            throw new NotFoundException("流程[" + defineKey + "]不存在");
        }
        //判断权限
        activityConfigurationService.getProcessConfiguration(definition.getId())
                .assertCanStartProcess(authentication.getUser().getId(), definition);

        String id = definition.getId();

        ProcessInstance instance = bpmProcessService.startProcessInstance(StartProcessRequest.builder()
                .creatorId(authentication.getUser().getId())
                .creatorName(authentication.getUser().getName())
                .formData(data)
                .processDefineId(id)
                .build());


        return ResponseMessage.ok(instance.getId());
    }

    /**
     * 提交表单数据并启动流程
     */
    @PostMapping("start/id/{defId}")
    @ApiOperation("提交表单数据并根据流程定义ID启动流程")
    @Authorize(merge = false)
    public ResponseMessage<String> startProcess(@PathVariable String defId,
                                                @RequestBody Map<String, Object> data,
                                                Authentication authentication) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(defId)
                .active()
                .singleResult();

        if (null == definition) {
            throw new NotFoundException("流程[" + defId + "]不存在");
        }
        //判断权限
        activityConfigurationService.getProcessConfiguration(definition.getId())
                .assertCanStartProcess(authentication.getUser().getId(), definition);


        ProcessInstance instance = bpmProcessService.startProcessInstance(StartProcessRequest.builder()
                .creatorId(authentication.getUser().getId())
                .creatorName(authentication.getUser().getName())
                .formData(data)
                .processDefineId(defId)
                .build());

        return ResponseMessage.ok(instance.getId());
    }

    /**
     * 获取待签收的任务
     */
    @GetMapping("claims")
    @ApiOperation("获取所有待签收的任务")
    @Authorize(merge = false)
    public ResponseMessage<List<Task>> getMyTasks(Authentication authentication) {

        List<Task> tasks = bpmTaskService.claimList(authentication.getUser().getId());

        return ResponseMessage.ok(tasks)
                .include(Task.class, "id", "name", "createTime", "executionId"
                        , "parentTaskId", "processInstanceId", "processDefinitionId", "taskDefinitionKey")
                .exclude(Task.class, "definition", "mainFormData");
    }

    @PutMapping("claim/{taskId}")
    @ApiOperation("签收任务")
    public ResponseMessage<Void> claim(@PathVariable String taskId, Authentication authentication) {
        bpmTaskService.claim(taskId, authentication.getUser().getId());
        return ResponseMessage.ok();
    }

    /**
     * 办理任务
     *
     * @param taskId 办理任务
     * @return 办理
     */
    @PutMapping("complete/{taskId}")
    public ResponseMessage<Map<String, Object>> complete(@PathVariable String taskId,
                                                         @RequestBody(required = false) Map<String, Object> formData,
                                                         Authentication authentication) {
        // 办理
        bpmTaskService.complete(CompleteTaskRequest.builder()
                .taskId(taskId)
                .completeUserId(authentication.getUser().getId())
                .completeUserName(authentication.getUser().getName())
                .formData(formData)
                .build());
        return ResponseMessage.ok();
    }
}
