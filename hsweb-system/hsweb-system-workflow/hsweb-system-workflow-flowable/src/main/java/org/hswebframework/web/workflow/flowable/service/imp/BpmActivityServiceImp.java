package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ActivityImpl getActivityByKey(String procDefKey, String activityId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
        String procDefId = definition.getId();
        return getProcessDefinition(procDefId).findActivity(activityId);
    }

    /**
     * 获取所有节点
     *
     * @param processDefId        流程定义ID
     * @return List<ActivityImpl>  当前流程的所有节点资源
     */
    @Override
    public List<ActivityImpl> getActivitys(String processDefId){
        ProcessDefinitionEntity pde = getProcessDefinition(processDefId);
        return pde.getActivities();
    }

    /**
     * 获取所有userTask
     *
     * @param procDefId        流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    @Override
    public List<ActivityImpl> getUserTasks(String procDefId){
        ProcessDefinitionEntity pde = getProcessDefinition(procDefId);
        List<ActivityImpl> activityList = new ArrayList<>();
        for(ActivityImpl activity : pde.getActivities()){
            if(activity.getProperty("type").equals("userTask"))
                activityList.add(activity);
        }
        return activityList;
    }

    /**
     * 获取结束节点
     * @param procDefId        流程定义ID
     * @return ActivityImpl       当前流程的结束资源
     */
    @Override
    public ActivityImpl getEndActivityImpl(String procDefId){
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
}
