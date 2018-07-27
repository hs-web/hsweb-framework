package org.hswebframework.web.workflow.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.hswebframework.web.workflow.service.WorkFlowFormService;
import org.hswebframework.web.workflow.service.config.CandidateInfo;
import org.hswebframework.web.workflow.service.config.ProcessConfigurationService;
import org.hswebframework.web.workflow.service.BpmProcessService;
import org.hswebframework.web.workflow.service.BpmTaskService;
import org.hswebframework.web.workflow.service.request.CompleteTaskRequest;
import org.hswebframework.web.workflow.service.request.JumpTaskRequest;
import org.hswebframework.web.workflow.service.request.RejectTaskRequest;
import org.hswebframework.web.workflow.service.request.StartProcessRequest;
import org.hswebframework.web.workflow.util.QueryUtils;
import org.hswebframework.web.workflow.web.response.CandidateDetail;
import org.hswebframework.web.workflow.web.response.ProcessInfo;
import org.hswebframework.web.workflow.web.response.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@RestController
@RequestMapping("/workflow/process")
@Api(tags = "工作流-流程管理", description = "工作流-流程管理")
@Authorize(permission = "workflow-process", description = "工作流-流程管理")
public class FlowableProcessController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private BpmTaskService bpmTaskService;

    @Autowired
    private BpmActivityService bpmActivityService;

    @Autowired
    private BpmProcessService bpmProcessService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessConfigurationService processConfigurationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private WorkFlowFormService workFlowFormService;

    @GetMapping("/doing")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("查询进行中的流程信息")
    public ResponseMessage<PagerResult<ProcessInfo>> queryProcess(QueryParamEntity query) {
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();

        PagerResult<ProcessInfo> result = QueryUtils.doQuery(instanceQuery, query, ProcessInfo::of, (term, q) -> {
            if ("status".equals(term.getColumn())) {
                switch (String.valueOf(term.getValue())) {
                    case "active":
                        q.active();
                        break;
                    case "suspended":
                        q.suspended();
                        break;
                    default:
                        break;
                }
            }
        });

        return ResponseMessage.ok(result).exclude(query.getExcludes()).include(query.getIncludes());
    }

    @GetMapping("/tasks")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("查询当前用户的历史任务信息")
    public ResponseMessage<PagerResult<TaskInfo>> getHistory(QueryParamEntity query, Authentication authentication) {
        HistoricTaskInstanceQuery historyQuery = historyService.createHistoricTaskInstanceQuery();
        historyQuery.taskAssignee(authentication.getUser().getId());

        PagerResult<TaskInfo> result = QueryUtils.doQuery(historyQuery, query, TaskInfo::of, (term, q) -> {
            if ("status".equals(term.getColumn())) {
                switch (String.valueOf(term.getValue())) {
                    case "finished":
                        q.finished();
                        break;
                    case "processFinished":
                        q.processFinished();
                        break;
                    default:
                        break;
                }
            }
        });

        return ResponseMessage.ok(result).exclude(query.getExcludes()).include(query.getIncludes());
    }

    @PostMapping("/start/key/{defineKey}")
    @ApiOperation("提交表单数据并根据流程定义key启动流程")
    @Authorize(merge = false)
    public ResponseMessage<String> startProcessByKey(@PathVariable String defineKey,
                                                     @RequestBody Map<String, Object> data,
                                                     Authentication authentication) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(defineKey)
                .active()
                .latestVersion()
                .singleResult();

        if (null == definition) {
            throw new NotFoundException("流程[" + defineKey + "]不存在");
        }
        //判断权限
        processConfigurationService.getProcessConfiguration(definition.getId())
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

    @PostMapping("/start/id/{defId}")
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
        processConfigurationService.getProcessConfiguration(definition.getId())
                .assertCanStartProcess(authentication.getUser().getId(), definition);


        ProcessInstance instance = bpmProcessService.startProcessInstance(StartProcessRequest.builder()
                .creatorId(authentication.getUser().getId())
                .creatorName(authentication.getUser().getName())
                .formData(data)
                .processDefineId(defId)
                .build());

        return ResponseMessage.ok(instance.getId());
    }

    @GetMapping("/todo")
    @ApiOperation("获取待办任务")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<TaskInfo>> getTodoList(QueryParamEntity query, Authentication authentication) {
        TaskQuery taskQuery = taskService.createTaskQuery();

        taskQuery.taskAssignee(authentication.getUser().getId());

        PagerResult<TaskInfo> result = QueryUtils.doQuery(taskQuery, query, TaskInfo::of);


        return ResponseMessage.ok(result).exclude(query.getExcludes()).include(query.getIncludes());
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        claim("user-wf-claim"),
        todo("user-wf-todo"),
        completed("user-wf-completed"),
        part("user-wf-part"),
        create("is") {
            @Override
            public void applyQueryTerm(NestConditional<?> conditional, String userId) {
                conditional.accept("creatorId", getTermType(), userId);
            }
        },
        claimOrTodo("is") {
            @Override
            public void applyQueryTerm(NestConditional<?> conditional, String userId) {
                conditional.nest()
                        .when(true, q -> Type.claim.applyQueryTerm(q, userId))
                        .or()
                        .when(true, q -> Type.todo.applyQueryTerm(q, userId))
                        .end();
            }
        };

        private String termType;

        public void applyQueryTerm(NestConditional<?> conditional, String userId) {
            conditional.accept("processInstanceId", termType, userId);
        }
    }

    @GetMapping("/{type}/form/{processDefineId}")
    @ApiOperation("获取自己可查看的流程表单数据")
    @Authorize(merge = false)
    @SuppressWarnings("all")
    public ResponseMessage<PagerResult<Object>> getFormData(@PathVariable Type type,
                                                            @PathVariable String processDefineId,
                                                            QueryParamEntity query,
                                                            Authentication authentication) {
        Query.empty(query)
                .nest()
                .when(type != null, q -> type.applyQueryTerm(q, authentication.getUser().getId()))
                .end();
        return ResponseMessage.ok(workFlowFormService.selectProcessForm(processDefineId, query));
    }

    @GetMapping("/task/form/{processDefineId}/{taskDefineKey}")
    @ApiOperation("获取流程任务表单数据")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<Object>> getTaskFormData(@PathVariable String processDefineId,
                                                                @PathVariable String taskDefineKey,
                                                                QueryParamEntity query) {
        return ResponseMessage.ok(workFlowFormService.selectTaskForm(processDefineId, taskDefineKey, query));
    }


    @GetMapping("/claims")
    @ApiOperation("获取待签收任务")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<TaskInfo>> getClaims(QueryParamEntity query, Authentication authentication) {
        TaskQuery taskQuery = taskService.createTaskQuery();

        taskQuery.taskCandidateUser(authentication.getUser().getId());

        PagerResult<TaskInfo> result = QueryUtils.doQuery(taskQuery, query, TaskInfo::of);

        return ResponseMessage.ok(result);
    }

    @GetMapping("/claims-and-todo")
    @ApiOperation("获取待签收和待处理的任务")
    @Authorize(merge = false)
    public ResponseMessage<PagerResult<TaskInfo>> getClaimsAndTodo(QueryParamEntity query, Authentication authentication) {
        TaskQuery taskQuery = taskService.createTaskQuery();

        taskQuery.taskCandidateOrAssigned(authentication.getUser().getId());

        PagerResult<TaskInfo> result = QueryUtils.doQuery(taskQuery, query, TaskInfo::of);

        return ResponseMessage.ok(result);
    }

    @PutMapping("/claim/{taskId}")
    @ApiOperation("签收任务")
    @Authorize(merge = false)
    public ResponseMessage<Void> claim(@PathVariable String taskId, Authentication authentication) {
        bpmTaskService.claim(taskId, authentication.getUser().getId());
        return ResponseMessage.ok();
    }

    @PutMapping("/complete/{taskId}")
    @Authorize(merge = false)
    public ResponseMessage<Void> complete(@PathVariable String taskId,
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

    @PutMapping("/reject/{taskId}")
    @Authorize(merge = false)
    @ApiOperation("驳回")
    public ResponseMessage<Void> reject(@PathVariable String taskId,
                                        @RequestBody Map<String, Object> data,
                                        Authentication authentication) {
        // 驳回
        bpmTaskService.reject(RejectTaskRequest.builder()
                .taskId(taskId)
                .rejectUserId(authentication.getUser().getId())
                .rejectUserName(authentication.getUser().getName())
                .data(data)
                .build());
        return ResponseMessage.ok();
    }

    @PutMapping("/jump/{taskId}/{activityId}")
    @Authorize(merge = false)
    @ApiOperation("流程跳转")
    public ResponseMessage<Void> jump(@PathVariable String taskId,
                                      @PathVariable String activityId,
                                      @RequestBody Map<String, Object> data,
                                      Authentication authentication) {
        // 流程跳转
        bpmTaskService.jumpTask(JumpTaskRequest
                .builder()
                .taskId(taskId)
                .targetActivityId(activityId)
                .recordLog(true)
                .jumpUserId(authentication.getUser().getId())
                .jumpUserName(authentication.getUser().getUsername())
                .data(data)
                .build());
        return ResponseMessage.ok();
    }

    @PostMapping("/next-task-candidate/{taskId}")
    @Authorize(merge = false)
    public ResponseMessage<List<CandidateDetail>> candidateList(@PathVariable String taskId,
                                                                @RequestBody Map<String, Object> data,
                                                                Authentication authentication) {

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityId(task.getTaskDefinitionKey())
                .singleResult();

        execution.setTransientVariables(data);

        List<TaskDefinition> taskDefinitions = bpmActivityService
                .getNextActivities(task.getProcessDefinitionId(), task.getTaskDefinitionKey(), (execution));

        List<CandidateDetail> candidates = taskDefinitions.stream().map(TaskDefinition::getKey)
                .flatMap(key ->
                        processConfigurationService
                                .getActivityConfiguration(authentication.getUser().getId(), task.getProcessDefinitionId(), key)
                                .getCandidateInfo(task)
                                .stream())
                .distinct()
                .map(CandidateDetail::of)
                .collect(Collectors.toList());


        return ResponseMessage.ok(candidates);
    }
}
