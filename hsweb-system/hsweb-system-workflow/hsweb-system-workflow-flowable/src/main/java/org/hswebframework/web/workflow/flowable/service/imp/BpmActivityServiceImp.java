package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
public class BpmActivityServiceImp extends FlowableAbstract implements BpmActivityService {

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

    public List<ActivityImpl> getActivitiesById(String processDefId, String activityId) {
        ProcessDefinitionEntity pde = getProcessDefinition(processDefId);
        if (activityId == null) {
            return pde.getActivities();
        } else {
            ActivityImpl activity = pde.findActivity(activityId);
            if (null == activity) return Collections.emptyList();
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

//        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
//        List<ActivityImpl> activityList = new ArrayList<>();
//        for (ActivityImpl activity : pde.getActivities()) {
//            if (activity.getProperty("type").equals("userTask"))
//                activityList.add(activity);
//        }
//        return activityList;
    }

    @Override
    public List<ActivityImpl> getUserTasksByProcDefKey(String procDefKey) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
        String procDefId = definition.getId();
        List<ActivityImpl> activities = findActivities(procDefId, activity -> "userTask".equals(activity.getProperty("type")));
//
//        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
//        List<ActivityImpl> activities = new ArrayList<>();
//        for (ActivityImpl activity : pde.getActivities()) {
//            if (activity.getProperty("type").equals("userTask"))
//                activities.add(activity);
//        }
        if (null != activities)
            activities.sort(Comparator.comparing(ProcessElementImpl::getId));
        return activities;
    }

    public List<TaskDefinition> getNextActivities(String procDefId, String activityId) {
        ActivityImpl activity;
        if (activityId != null)
            activity = getActivityById(procDefId, activityId);
        else
            activity = getStartEvent(procDefId);

        List<PvmTransition> pvmTransitions = activity.getOutgoingTransitions();

        return pvmTransitions.stream()
                .map(PvmTransition::getDestination)
                .map(ActivityImpl.class::cast)          //强转为ActivityImpl
                .filter(Objects::nonNull)
                .map(act -> getTaskDefinition(act, "")) //获取TaskDefinition集合
                .flatMap(Collection::stream)            //合并集合
                .collect(Collectors.toList());

//        List<TaskDefinition> taskDefinitions = new ArrayList<>();
//        for (PvmTransition pvmTransition : pvmTransitions) {
//            PvmActivity pvmActivity = pvmTransition.getDestination();
//            taskDefinitions.addAll(getTaskDefinition((ActivityImpl) pvmActivity, ""));
//        }
//        return taskDefinitions;
    }

    @Override
    public List<TaskDefinition> getTaskDefinition(ActivityImpl activityImpl, String elString) {
        List<TaskDefinition> taskDefinitionList = new ArrayList<>();
        List<TaskDefinition> nextTaskDefinition;
        if ("userTask".equals(activityImpl.getProperty("type"))) {
            TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityImpl.getActivityBehavior()).getTaskDefinition();
            taskDefinitionList.add(taskDefinition);
        } else {
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp;
            for (PvmTransition tr : pvmTransitions) {
                PvmActivity pvmActivity = tr.getDestination(); //获取线路的终点节点
                if ("exclusiveGateway".equals(pvmActivity.getProperty("type")) || "parallelGateway".equals(pvmActivity.getProperty("type"))) {
                    outTransitionsTemp = pvmActivity.getOutgoingTransitions();
                    if (outTransitionsTemp.size() == 1) {
                        nextTaskDefinition = getTaskDefinition((ActivityImpl) outTransitionsTemp.get(0).getDestination(), elString);
                        taskDefinitionList.addAll(nextTaskDefinition);
                    } else if (outTransitionsTemp.size() > 1) {
                        for (PvmTransition tr1 : outTransitionsTemp) {
                            Object s = tr1.getProperty("conditionText");
                            if (elString.equals(s.toString().trim())) {
                                nextTaskDefinition = getTaskDefinition((ActivityImpl) tr1.getDestination(), elString);
                                taskDefinitionList.addAll(nextTaskDefinition);
                            }
                        }
                    }
                } else if ("userTask".equals(pvmActivity.getProperty("type"))) {
                    taskDefinitionList.add(((UserTaskActivityBehavior) ((ActivityImpl) pvmActivity).getActivityBehavior()).getTaskDefinition());
                }
            }
        }
        return taskDefinitionList;
    }

    @Override
    public Map<String, List<String>> getNextClaim(String procDefId, String activityId) {
        List<TaskDefinition> taskDefinitions = getNextActivities(procDefId, activityId);
        Map<String, List<String>> map = new HashMap<>();
        for (TaskDefinition taskDefinition : taskDefinitions) {
            List<String> list = new ArrayList<>();
            if (taskDefinition.getAssigneeExpression() != null)
                list.add(taskDefinition.getAssigneeExpression().getExpressionText());
            else if (taskDefinition.getCandidateUserIdExpressions() != null) {
                for (Expression expression : taskDefinition.getCandidateUserIdExpressions()) {
                    list.add(expression.getExpressionText());
                }
            }
            if (taskDefinition.getNameExpression() != null)
                map.put(taskDefinition.getNameExpression().getExpressionText(), list);
            else
                map.put(taskDefinition.getKey(), list);
        }
        return map;
    }

    @Override
    public ActivityImpl getStartEvent(String procDefId) {
        return findActivity(procDefId, activity -> "startEvent".equals(activity.getProperty("type")));

//        List<ActivityImpl> activities = getActivitiesById(procDefId, null);
//        ActivityImpl activity = null;
//        for (ActivityImpl a : activities) {
//            if (a.getProperty("type").equals("startEvent")) {
//                activity = a;
//            }
//        }
//        return activity;
    }

    private List<ActivityImpl> findActivities(String procDefId, Predicate<ActivityImpl> predicate) {
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        if (pde == null) return null;
        return pde.getActivities()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private ActivityImpl findActivity(String procDefId, Predicate<ActivityImpl> predicate) {
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        if (pde == null) return null;
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
