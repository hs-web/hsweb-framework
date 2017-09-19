package org.hswebframework.web.workflow.flowable.service;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 流程任务操作相关接口
 * @Author wangwei
 * @Date 2017/8/4.
 */
public interface BpmTaskService{

    List<Task> selectNowTask(String procInstId);

    List<Task> selectTaskByProcessId(String procInstId);

    Task selectTaskByTaskId(String taskId);

    String selectNowTaskName(String procInstId);

    String selectNowTaskId(String procInstId);

    HistoricProcessInstance selectHisProInst(String procInstId);

    /**
     * 获取环节变量
     * @param taskId
     * @return
     */
    Map<String, Object> selectVariableLocalByTaskId(String taskId);

    /**
     * 获取环节变量
     * @param taskId
     * @param variableName
     * @return
     */
    Object selectVariableLocalByTaskId(String taskId, String variableName);

    /**
     * 根据taskId获取流程图对应的图元
     * @param taskId
     * @return
     */
    ActivityImpl selectActivityImplByTask(String taskId);

    /**
     * 领取（签收）任务
     *
     * @param taskId 任务id
     * @param userId 用户id
     * @throws Exception 异常信息
     */
    void claim(String taskId, String userId);


    /**
     * 预留等待签收的任务
     *
     * @param userId 用户id
     * @return 任务信息
     * @throws Exception
     */
    List<Task> claimList(String userId);

    /**
     * 已签收待办理的任务
     *
     * @param userId 用户id
     * @return 任务信息
     * @throws Exception
     */
    List<Task> todoList(String userId);

    /**
     * 完成任务（环节）并指定下一环节办理人
     *
     * @param taskId    任务id
     * @param userId    当前办理人用户id
     * @param activityId 人为指定下一执行环节（预留）
     * @param next_claim 人为指定下一步执行人（预留）
     * @throws Exception 异常信息
     */
    void complete(String taskId, String userId, String activityId, String next_claim);

    /**
     * 流程任意跳转
     * @param procInstId  流程实例ID
     * @param activity           流程环节ID
     * @param next_claim         人为指定下一步执行人（预留）
     */
    void jumpTask(String procInstId, String activity, String next_claim);

    /**
     * 驳回
     * @param taskId
     */
    void reject(String taskId);

    /**
     * 设置待办人
     *
     * @param taskId   当前环节ID
     * @param actId   当前环节图元ID
     * @param userId 用户ID
     */
    void addCandidateUser(String taskId, String actId, String userId);

    /**
     * 设置办理人
     *
     * @param taskId   当前环节ID
     * @param userId 用户ID
     */
    void setAssignee(String taskId, String userId);

    /**
     * 结束流程
     * @param procInstId   流程实例ID
     */
    void endProcess(String procInstId);

    /**
     * 删除历史环节信息
     * @param taskId  环节ID
     */
    void removeHiTask(String taskId);

    /**
     * 获取所有任务节点
     * @param procDefKey  流程定义Key，该参数获取最新流程
     */
    Map<String,Object> getUserTasksByProcDefKey(String procDefKey);

    /**
     * 获取所有任务节点
     * @param procInstId            流程实例ID，该参数获取当前流程实例对应流程
     */
    Map<String,Object> getUserTasksByProcInstId(String procInstId);

    /**
     * 设置流程变量
     *
     * @param taskId   当前环节ID
     * @param map   key-value
     */
    void setVariables(String taskId,Map<String ,Object> map);

    /**
     * 删除流程变量
     *
     * @param taskId   当前环节ID
     * @param var2   需要删除的 key
     */
    void removeVariables(String taskId,Collection<String> var2);

    /**
     * 设置任务变量
     *
     * @param taskId   当前环节ID
     */
    void setVariablesLocal(String taskId, Map<String, Object> map);

    /***
     * 获取流程变量
     * @param procInstId  流程实例ID
     */
    Map<String,Object> getVariablesByProcInstId(String procInstId);

    /**
     * 获取流程变量
     * @param taskId
     * @return
     */
    Map<String,Object> getVariablesByTaskId(String taskId);
}
