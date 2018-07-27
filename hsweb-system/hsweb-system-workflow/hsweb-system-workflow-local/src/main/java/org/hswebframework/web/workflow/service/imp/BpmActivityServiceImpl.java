package org.hswebframework.web.workflow.service.imp;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExpressionGetInvocation;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
public class BpmActivityServiceImpl extends AbstractFlowableService implements BpmActivityService {

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 获取指定节点
     *
     * @param procDefId 流程定义ID
     * @return ActivityImpl       指定的节点资源
     */
    @Override
    public ActivityImpl getActivityById(String procDefId, String activityId) {
        return getProcessDefinition(procDefId).findActivity(activityId);
    }

    @Override
    public List<ActivityImpl> getActivitiesByKey(String procDefKey, String activityId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(procDefKey)
                .orderByProcessDefinitionVersion()
                .desc()
                .singleResult();

        return getActivitiesById(definition.getId(), activityId);
    }

    @Override
    public List<ActivityImpl> getActivitiesById(String processDefId, String activityId) {
        ProcessDefinitionEntity pde = getProcessDefinition(processDefId);
        if (activityId == null) {
            return pde.getActivities();
        } else {
            ActivityImpl activity = pde.findActivity(activityId);
            if (null == activity) {
                return new java.util.ArrayList<>();
            }
            return Collections.singletonList(activity);
        }
    }

    /**
     * 获取所有userTask
     *
     * @param procDefId 流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    @Override
    public List<ActivityImpl> getUserTasksByProcDefId(String procDefId) {

        return findActivities(procDefId, activity -> "userTask".equals(activity.getProperty("type")));

    }

    @Override
    public List<ActivityImpl> getUserTasksByProcDefKey(String procDefKey) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
        String procDefId = definition.getId();
        List<ActivityImpl> activities = findActivities(procDefId, activity -> "userTask".equals(activity.getProperty("type")));
//
        if (null != activities) {
            activities.sort(Comparator.comparing(ProcessElementImpl::getId));
        }
        return activities;
    }

    @Override
    public List<TaskDefinition> getNextActivities(String procDefId, String activityId, DelegateExecution execution) {
        ActivityImpl activity;
        if (activityId != null) {
            activity = getActivityById(procDefId, activityId);
        } else {
            activity = getStartEvent(procDefId);
        }

        List<PvmTransition> pvmTransitions = activity.getOutgoingTransitions();

        return pvmTransitions.stream()
                .map(PvmTransition::getDestination)
                .map(ActivityImpl.class::cast)          //强转为ActivityImpl
                .filter(Objects::nonNull)
                .map(act -> getTaskDefinition(act, execution)) //获取TaskDefinition集合
                .flatMap(Collection::stream)            //合并集合
                .collect(Collectors.toList());

    }

    @Override
    public List<TaskDefinition> getTaskDefinition(ActivityImpl activityImpl, DelegateExecution execution) {
        Set<TaskDefinition> taskDefinitionList = new HashSet<>();
        List<TaskDefinition> nextTaskDefinition;
        if ("userTask".equals(activityImpl.getProperty("type"))) {
            TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityImpl.getActivityBehavior()).getTaskDefinition();
            taskDefinitionList.add(taskDefinition);
        } else {
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp;
            for (PvmTransition tr : pvmTransitions) {
                PvmActivity pvmActivity = tr.getSource(); //获取线路的终点节点

                boolean exclusiveGateway = "exclusiveGateway".equals(pvmActivity.getProperty("type"));
                boolean parallelGateway = "parallelGateway".equals(pvmActivity.getProperty("type"));

                if (exclusiveGateway || parallelGateway) {
                    outTransitionsTemp = pvmActivity.getOutgoingTransitions();
                    if (outTransitionsTemp.size() == 1) {
                        nextTaskDefinition = getTaskDefinition((ActivityImpl) outTransitionsTemp.get(0).getDestination(), execution);
                        taskDefinitionList.addAll(nextTaskDefinition);
                    } else if (outTransitionsTemp.size() > 1) {
                        for (PvmTransition transition : outTransitionsTemp) {
                            String condition = (String) transition.getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT);
                            if (StringUtils.isEmpty(condition)) {
                                nextTaskDefinition = getTaskDefinition((ActivityImpl) transition.getDestination(), execution);
                                if (exclusiveGateway) {
                                    if (!CollectionUtils.isEmpty(nextTaskDefinition)) {
                                        taskDefinitionList.add(nextTaskDefinition.get(0));
                                    }
                                } else {
                                    taskDefinitionList.addAll(nextTaskDefinition);
                                }
                                continue;
                            }
                            ExpressionManager expressionManager = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getExpressionManager();

                            ELContext elContext = expressionManager.getElContext(execution);

                            ExpressionFactoryImpl factory = new ExpressionFactoryImpl();

                            Object e = factory.createValueExpression(elContext, condition, Object.class).getValue(elContext);

                            if (Boolean.TRUE.equals(e)) {
                                nextTaskDefinition = getTaskDefinition((ActivityImpl) transition.getDestination(), execution);
                                taskDefinitionList.addAll(nextTaskDefinition);
                            }
                        }
                    }
                } else if ("userTask".equals(pvmActivity.getProperty("type"))) {
                    taskDefinitionList.add(((UserTaskActivityBehavior) ((ActivityImpl) pvmActivity).getActivityBehavior()).getTaskDefinition());
                }
            }
        }
        return new ArrayList<>(taskDefinitionList);
    }

    @Override
    public ActivityImpl getStartEvent(String procDefId) {
        return findActivity(procDefId, activity -> "startEvent".equals(activity.getProperty("type")));
    }

    private List<ActivityImpl> findActivities(String procDefId, Predicate<ActivityImpl> predicate) {
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        if (pde == null) {
            return new ArrayList<>();
        }
        return pde.getActivities()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private ActivityImpl findActivity(String procDefId, Predicate<ActivityImpl> predicate) {
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        if (pde == null) {
            return null;
        }
        return pde.getActivities()
                .stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    @Override
    public ActivityImpl getEndEvent(String procDefId) {
        return findActivity(procDefId, activity -> "endEvent".equals(activity.getProperty("type")));
    }

    /**
     * 获取流程定义
     *
     * @param procDefId 流程定义ID
     * @return 流程定义资源
     */
    public ProcessDefinitionEntity getProcessDefinition(String procDefId) {
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(procDefId);
    }

    @Override
    public ActivityImpl getActivityByProcInstId(String procDefId, String procInstId) {
        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(procInstId)
                .active()
                .singleResult();
        String activityId = processInstance.getActivityId();

        return getProcessDefinition(procDefId).findActivity(activityId);
    }

    @Override
    public List<ActivityImpl> getNextEvent(ActivityImpl activity) {

        return activity.getOutgoingTransitions()
                .stream()
                .map(PvmTransition::getDestination)
                .map(ActivityImpl.class::cast)
                .collect(Collectors.toList());

//        List<ActivityImpl> activities = new ArrayList<>();
//        List<PvmTransition> pvmTransitions = activity.getOutgoingTransitions();
//        for (PvmTransition pvmTransition : pvmTransitions) {
//            activities.add((ActivityImpl) pvmTransition.getDestination());
//        }
//        return activities;
    }
}
