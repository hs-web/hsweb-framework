package org.hswebframework.web.workflow.service;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;

import java.util.List;
import java.util.Map;

/**
 * 流程节点操作的接口
 *
 * @Author wangwei
 * @Date 2017/8/4.
 */
public interface BpmActivityService {
    /**
     * 获取指定节点
     *
     * @param procDefId 流程定义ID
     * @return ActivityImpl       指定的节点资源,未指定返回第一节点
     */
    ActivityImpl getActivityById(String procDefId, String activityId);

    /**
     * 获取所有节点
     *
     * @param procDefKey 流程定义Key
     * @param activityId 图元ID
     * @return ActivityImpl       指定的节点,未指定返回所有
     */
    List<ActivityImpl> getActivitiesByKey(String procDefKey, String activityId);

    /**
     * 获取所有节点
     *
     * @param procDefId  流程定义ID
     * @param activityId 图元ID
     * @return List<ActivityImpl>  当前流程的所有节点资源
     */
    List<ActivityImpl> getActivitiesById(String procDefId, String activityId);

    /**
     * 获取所有userTask
     *
     * @param procDefId 流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    List<ActivityImpl> getUserTasksByProcDefId(String procDefId);

    /**
     * 获取所有userTask
     *
     * @param procDefKey 流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    List<ActivityImpl> getUserTasksByProcDefKey(String procDefKey);

    /**
     * 获取下一环节
     *
     * @param procDefId  流程定义ID
     * @param activityId 图元ID
     * @return List<TaskDefinition>  当前流程的所有下一环节资源
     */
    List<TaskDefinition> getNextActivities(String procDefId, String activityId,DelegateExecution execution);


    /**
     * 根据图元获取办理环节数据
     *
     * @param activityImpl
     * @param execution     根据连线条件conditionText获取输出节点，主要用于网关分支（预留）
     * @return
     */
    List<TaskDefinition> getTaskDefinition(ActivityImpl activityImpl,DelegateExecution execution);

    /**
     * 获取开始节点
     *
     * @param procDefId 流程定义ID
     * @return ActivityImpl       当前流程的结束资源
     */
    ActivityImpl getStartEvent(String procDefId);

    /**
     * 获取结束节点
     *
     * @param procDefId 流程定义ID
     * @return ActivityImpl       当前流程的结束资源
     */
    ActivityImpl getEndEvent(String procDefId);

    /***
     * 获取当前执行节点
     * @param procDefId     流程定义ID
     * @param procInstId    流程实例ID
     * @return
     */
    ActivityImpl getActivityByProcInstId(String procDefId, String procInstId);

    /**
     * 根据节点获取下一步执行节点集合
     *
     * @param activity
     * @return
     */
    List<ActivityImpl> getNextEvent(ActivityImpl activity);
}
