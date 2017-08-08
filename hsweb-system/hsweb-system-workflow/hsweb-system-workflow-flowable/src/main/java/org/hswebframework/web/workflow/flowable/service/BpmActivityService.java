package org.hswebframework.web.workflow.flowable.service;

import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.List;

/**
 * 流程节点操作的接口
 * @Author wangwei
 * @Date 2017/8/4.
 */
public interface BpmActivityService {
    /**
     * 获取指定节点
     *
     * @param procDefId        流程定义ID
     * @return ActivityImpl       指定的节点资源
     */
    ActivityImpl getActivityById(String procDefId, String activityId);

    /**
     * 获取指定节点
     *
     * @param procDefKey        流程定义Key
     * @return ActivityImpl       指定的节点,未指定返回当前节点
     */
    ActivityImpl getActivityByKey(String procDefKey, String activityId);

    /**
     * 获取所有节点
     *
     * @param procDefId        流程定义ID
     * @return List<ActivityImpl>  当前流程的所有节点资源
     */
    List<ActivityImpl> getActivitys(String procDefId);

    /**
     * 获取所有userTask
     *
     * @param procDefId        流程定义ID
     * @return List<ActivityImpl>  当前流程的所有userTask资源
     */
    List<ActivityImpl> getUserTasks(String procDefId);

    /**
     * 获取结束节点
     * @param procDefId        流程定义ID
     * @return ActivityImpl       当前流程的结束资源
     */
    ActivityImpl getEndActivityImpl(String procDefId);

    /***
     * 获取当前执行节点
     * @param procDefId     流程定义ID
     * @param procInstId    流程实例ID
     * @return
     */
    ActivityImpl getActivityByProcInstId(String procDefId, String procInstId);
}
