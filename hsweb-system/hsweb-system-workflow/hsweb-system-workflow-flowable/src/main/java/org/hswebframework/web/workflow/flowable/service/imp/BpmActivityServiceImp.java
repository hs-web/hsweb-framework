package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
public class BpmActivityServiceImp extends FlowableAbstract implements BpmActivityService {

    /**
     * 获取指定节点
     *
     * @param procDefId        流程定义ID
     * @return ActivityImpl       指定的节点资源
     */
    @Override
    public ActivityImpl getActivityById(String procDefId, String activityId){
        return getProcessDefinition(procDefId).findActivity(activityId);
    }

    @Override
    public List<ActivityImpl> getActivityByKey(String procDefKey, String activityId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
        String procDefId = definition.getId();
        if (activityId == null) {
            return getProcessDefinition(procDefId).getActivities();
        }else {
            List<ActivityImpl> activities = new ArrayList<>();
            activities.add(getProcessDefinition(procDefId).findActivity(activityId));
            return activities;
        }
    }

    @Override
    public List<ActivityImpl> getActivitysById(String processDefId, String activityId){
        ProcessDefinitionEntity pde = getProcessDefinition(processDefId);
        if (activityId == null) {
            return pde.getActivities();
        }else {
            List<ActivityImpl> activities = new ArrayList<>();
            activities.add(pde.findActivity(activityId));
            return activities;
        }
    }

    /**
     * 获取所有userTask
     *
     * @param procDefId        流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    @Override
    public List<ActivityImpl> getUserTasksByProcDefId(String procDefId){
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        List<ActivityImpl> activityList = new ArrayList<>();
        for(ActivityImpl activity : pde.getActivities()){
            if(activity.getProperty("type").equals("userTask"))
                activityList.add(activity);
        }
        return activityList;
    }

    @Override
    public List<ActivityImpl> getUserTasksByProcDefKey(String procDefKey) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
        String procDefId = definition.getId();
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        List<ActivityImpl> activities = new ArrayList<>();
        for(ActivityImpl activity : pde.getActivities()){
            if(activity.getProperty("type").equals("userTask"))
                activities.add(activity);
        }
        Collections.sort(activities, new Comparator<ActivityImpl>() {
            @Override
            public int compare(ActivityImpl o1, ActivityImpl o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return activities;
    }

    @Override
    public ActivityImpl getStartEvent(String procDefKey) {
        List<ActivityImpl> activities = getActivityByKey("test",null);
        ActivityImpl activity = null;
        for (ActivityImpl a: activities) {
            if(a.getProperty("type").equals("startEvent")){
                activity = a;
            }
        }
        return activity;
    }

    @Override
    public ActivityImpl getEndEvent(String procDefId){
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        for(ActivityImpl activity : pde.getActivities()){
            if(activity.getProperty("type").equals("endEvent"))
                return activity;
        }
        return null;
    }

    /**
     * 获取流程定义
     *
     * @param procDefId        流程定义ID
     * @return 流程定义资源
     */
    public ProcessDefinitionEntity getProcessDefinition(String procDefId){
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(procDefId);
    }

    @Override
    public ActivityImpl getActivityByProcInstId(String procDefId, String procInstId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId)
                .active().singleResult();
        String activityId = processInstance.getActivityId();

        return getProcessDefinition(procDefId).findActivity(activityId);
    }

    @Override
    public List<ActivityImpl> getNextEvent(ActivityImpl activity) {
        List<ActivityImpl> activities = new ArrayList<>();
        List<PvmTransition> pvmTransitions = activity.getOutgoingTransitions();
        for(PvmTransition pvmTransition : pvmTransitions){
            activities.add((ActivityImpl)pvmTransition.getDestination());
        }
        return activities;
    }
}
