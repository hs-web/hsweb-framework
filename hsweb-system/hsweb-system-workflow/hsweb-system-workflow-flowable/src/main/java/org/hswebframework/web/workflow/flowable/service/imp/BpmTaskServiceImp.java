package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.entity.workflow.ActDefEntity;
import org.hswebframework.web.organizational.authorization.Personnel;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.service.organizational.PersonService;
import org.hswebframework.web.service.organizational.RelationDefineService;
import org.hswebframework.web.service.organizational.RelationInfoService;
import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.service.BpmTaskService;
import org.hswebframework.web.workflow.flowable.service.BpmUtilsService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.hswebframework.web.workflow.flowable.utils.JumpTaskCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.hswebframework.web.commons.entity.param.QueryParamEntity.single;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class BpmTaskServiceImp extends FlowableAbstract implements BpmTaskService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private BpmActivityService bpmActivityService;
    @Autowired
    ActDefService actDefService;
    @Autowired
    BpmUtilsService bpmUtilsService;

    @Override
    public List<Task> selectNowTask(String procInstId) {
        return taskService.createTaskQuery().processInstanceId(procInstId).active().list();
    }

    @Override
    public List<Task> selectTaskByProcessId(String procInstId) {
        return taskService.createTaskQuery().processInstanceId(procInstId).active().list();
    }

    @Override
    public Task selectTaskByTaskId(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).active().singleResult();
    }

    @Override
    public String selectNowTaskName(String procInstId) {
        List<Task> tasks = selectNowTask(procInstId);
        if (tasks.size() == 1)
            return tasks.get(0).getName();
        else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tasks.size(); i++) {
                if (i != 0)
                    builder.append(",");
                builder.append(tasks.get(i).getName());
            }
            return builder.toString();
        }

    }

    @Override
    public String selectNowTaskId(String procInstId) {
        List<Task> tasks = selectNowTask(procInstId);
        if (tasks.size() == 1)
            return tasks.get(0).getId();
        else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tasks.size(); i++) {
                if (i != 0)
                    builder.append(",");
                builder.append(tasks.get(i).getId());
            }
            return builder.toString();
        }
    }

    @Override
    public void claim(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).taskCandidateUser(userId).active().singleResult();
        if (task == null) {
            logger.warn("获取任务失败!");
            throw new NotFoundException("task not found");
            //return; // fix null point
        }
        if (!StringUtils.isNullOrEmpty(task.getAssignee())) {
            logger.warn("该任务已被签收!");
        } else taskService.claim(taskId, userId);
    }


    @Override
    public List<Task> claimList(String userId) {
        // 等待签收的任务
        List<Task> claimList = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .includeProcessVariables()
                .active()
                .list();
        return claimList;
    }

    @Override
    public List<Task> todoList(String userId) {
        // 已经签收的任务
        List<Task> todoList = taskService.createTaskQuery()
                .taskAssignee(userId)
                .includeProcessVariables()
                .active()
                .list();
        return todoList;
    }

    @Override
    public void complete(String taskId, String userId, String activityId, String nextClaim) {
        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().active().singleResult();
        if (StringUtils.isNullOrEmpty(task)) {
            logger.warn("任务不存在!");
            throw new NotFoundException("task not found");
        }
        String assignee = task.getAssignee();
        if (StringUtils.isNullOrEmpty(assignee)) {
            logger.warn("请先签收任务!");
            throw new NotFoundException("Please sign for the task first");
        }
        if (!userId.equals(assignee)) {
            logger.warn("只能完成自己的任务");
            throw new NotFoundException("You can only do your own work");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("oldTaskId", task.getId());
        //完成此任务
        if (StringUtils.isNullOrEmpty(activityId)) {
            taskService.complete(taskId, map);
        } else {
            jumpTask(taskId, activityId, nextClaim);
        }
        //根据流程ID查找执行计划，存在则进行下一步,没有则结束（定制化流程预留）
        List<Execution> execution = runtimeService.createExecutionQuery().processInstanceId(task.getProcessInstanceId()).list();
        if (execution.size() > 0) {
            List<Task> tasks = selectNowTask(task.getProcessInstanceId());
            // 自定义下一执行人
            if (tasks.size() == 1 && !StringUtils.isNullOrEmpty(nextClaim)) claim(tasks.get(0).getId(), nextClaim);
            else {
                for (Task t : tasks) {
                    addCandidateUser(t.getId(), t.getTaskDefinitionKey(), userId);
                }
            }
        }
    }

    @Override
    public void reject(String taskId) {
        // 先判定是否存在历史环节
        String oldTaskId = selectVariableLocalByTaskId(taskId, "oldTaskId").toString();
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().taskId(oldTaskId).singleResult();
        if (taskInstance == null) {
            throw new NotFoundException("历史任务环节不存在,taskId:" + oldTaskId);
        }

        Task task = selectTaskByTaskId(taskId);

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        if (processInstance == null) {
            throw new NotFoundException("流程已经结束");
        }

        Map<String, Object> variables = processInstance.getProcessVariables();

        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(task.getProcessDefinitionId());
        if (definition == null) {
            throw new NotFoundException("流程定义未找到");
        }

        ActivityExecution execution = (ActivityExecution) runtimeService.createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();
        // 是否并行节点
        if (execution.isConcurrent()) {
            throw new NotFoundException("并行节点不允许驳回,taskId:" + task.getId());
        }

        // 是否存在定时任务
        long num = managementService.createJobQuery().executionId(task.getExecutionId()).count();
        if (num > 0) throw new NotFoundException("当前环节不允许驳回");

        // 驳回


        // 取得上一步活动
        ActivityImpl currActivity = definition.findActivity(task.getTaskDefinitionKey());
        List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();
        // 清除当前活动的出口
        List<PvmTransition> oriPvmTransitionList = new ArrayList<>();
        List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
        oriPvmTransitionList.addAll(pvmTransitionList);
        pvmTransitionList.clear();

        // 建立新出口
        List<TransitionImpl> newTransitions = new ArrayList<>();
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getSource();
            ActivityImpl nextActivityImpl = definition.findActivity(nextActivity.getId());
            TransitionImpl newTransition = currActivity.createOutgoingTransition();
            newTransition.setDestination(nextActivityImpl);
            newTransitions.add(newTransition);
        }
        // 完成任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey(task.getTaskDefinitionKey()).list();
        for (Task t : tasks) {
            taskService.complete(t.getId(), variables);
            historyService.deleteHistoricTaskInstance(t.getId());
        }
        // 恢复方向
        for (TransitionImpl transitionImpl : newTransitions) {
            currActivity.getOutgoingTransitions().remove(transitionImpl);
        }
        pvmTransitionList.addAll(oriPvmTransitionList);

    }

    @Override
    public void jumpTask(String taskId, String activity, String next_claim) {
        Task task = selectTaskByTaskId(taskId);
        TaskServiceImpl taskServiceImpl = (TaskServiceImpl) taskService;
        taskServiceImpl.getCommandExecutor().execute(new JumpTaskCmd(task.getExecutionId(), activity));
        if(!StringUtils.isNullOrEmpty(next_claim)){
            task = selectTaskByTaskId(taskId);
            if (null != task)
                claim(task.getId(), next_claim);
        }
    }

    @Override
    public void addCandidateUser(String taskId, String actId, String userId) {
        if (!StringUtils.isNullOrEmpty(actId)) {
            // 获取节点配置信息
            ActDefEntity actDefEntity = actDefService.selectSingle(single(ActDefEntity.actId, actId));
            // 根据配置类型  获取人员信息 设置待办人
            if (actDefEntity!=null) {
                List<String> list = bpmUtilsService.selectUserIdsBy(userId,actDefEntity);
                list.forEach(uId -> taskService.addCandidateUser(taskId,uId));
            } else {
                taskService.addCandidateUser(taskId,
                        runtimeService.getIdentityLinksForProcessInstance(selectTaskByTaskId(taskId).getProcessInstanceId())
                        .stream()
                        .filter(linkEntity -> linkEntity.getType().equals("starter"))
                        .findFirst().orElseThrow(()-> new NotFoundException("发起人获取失败")).getUserId()
                );
            }
        } else {
            taskService.addCandidateUser(taskId, userId);
        }
    }

    @Override
    public void setAssignee(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    @Override
    public void endProcess(String procInstId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        ActivityImpl activity = bpmActivityService.getEndEvent(processInstance.getProcessDefinitionId());
        jumpTask(procInstId, activity.getId(), null);
    }

    @Override
    public void removeHiTask(String taskId) {
        historyService.deleteHistoricTaskInstance(taskId);
    }

    @Override
    public Map<String, Object> selectVariableLocalByTaskId(String taskId) {
        return taskService.getVariablesLocal(taskId);
    }

    @Override
    public Object selectVariableLocalByTaskId(String taskId, String variableName) {
        return taskService.getVariableLocal(taskId, variableName);
    }

    @Override
    public HistoricProcessInstance selectHisProInst(String procInstId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
    }

    @Override
    public ActivityImpl selectActivityImplByTask(String taskId) {
        if (StringUtils.isNullOrEmpty(taskId)) {
            return new ActivityImpl(null, null);
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessDefinitionEntity entity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(task.getProcessDefinitionId());
        List<ActivityImpl> activities = entity.getActivities();
        return activities
                .stream()
                .filter(activity -> "userTask".equals(activity.getProperty("type")) && activity.getProperty("name").equals(task.getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("获取节点信息失败"));
    }

    @Override
    public Map<String, Object> getUserTasksByProcDefKey(String procDefKey) {
        String definitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0).getId();
        List<ActivityImpl> activities = bpmActivityService.getUserTasksByProcDefId(definitionId);
        Map<String, Object> map = new HashMap<>();
        for (ActivityImpl activity : activities) {
            map.put(activity.getId(), activity.getProperty("name"));
        }
        return map;
    }

    @Override
    public Map<String, Object> getUserTasksByProcInstId(String procInstId) {
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult().getProcessDefinitionId();
        List<ActivityImpl> activities = bpmActivityService.getUserTasksByProcDefId(definitionId);
        Map<String, Object> map = new HashMap<>();
        for (ActivityImpl activity : activities) {
            map.put(activity.getId(), activity.getProperty("name"));
        }
        return map;
    }

    @Override
    public void setVariables(String taskId, Map<String, Object> map) {
        taskService.setVariables(taskId, map);
    }

    @Override
    public void removeVariables(String taskId, Collection<String> var2) {
        taskService.removeVariables(taskId, var2);
    }

    @Override
    public void setVariablesLocal(String taskId, Map<String, Object> map) {
        taskService.setVariablesLocal(taskId, map);
    }

    @Override
    public Map<String, Object> getVariablesByProcInstId(String procInstId) {
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(procInstId).list();
        String executionId = "";
        for (Execution execution : executions) {
            if (StringUtils.isNullOrEmpty(execution.getParentId())) {
                executionId = execution.getId();
            }
        }
        return runtimeService.getVariables(executionId);
    }

    @Override
    public Map<String, Object> getVariablesByTaskId(String taskId) {
        return taskService.getVariables(taskId);
    }
}
