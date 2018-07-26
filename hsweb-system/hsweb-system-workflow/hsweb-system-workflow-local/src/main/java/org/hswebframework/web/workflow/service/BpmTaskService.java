package org.hswebframework.web.workflow.service;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;
import org.hswebframework.web.workflow.service.request.CompleteTaskRequest;
import org.hswebframework.web.workflow.service.request.JumpTaskRequest;
import org.hswebframework.web.workflow.service.request.RejectTaskRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 流程任务操作相关接口
 *
 * @Author wangwei
 * @Date 2017/8/4.
 */
public interface BpmTaskService {

    List<Task> selectNowTask(String procInstId);

    List<Task> selectTaskByProcessId(String procInstId);

    Task selectTaskByTaskId(String taskId);

    HistoricProcessInstance selectHisProInst(String procInstId);

    /**
     * 设置任务办理的候选人
     *
     * @param doingUserId 当前操作人
     * @param task        任务
     */
    void setCandidate(String doingUserId, Task task);

    /**
     * 获取环节变量
     *
     * @param taskId
     * @return
     */
    Map<String, Object> selectVariableLocalByTaskId(String taskId);

    /**
     * 获取环节变量
     *
     * @param taskId
     * @param variableName
     * @return
     */
    Object selectVariableLocalByTaskId(String taskId, String variableName);

    /**
     * 根据taskId获取流程图对应的图元
     *
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
     * 完成任务
     *
     * @param request 完成任务请求
     */
    void complete(CompleteTaskRequest request);

    /**
     * 流程任意跳转
     */
    void jumpTask(JumpTaskRequest request);

    /**
     * 驳回任务
     */
    void reject(RejectTaskRequest request);

    /**
     * 结束流程
     *
     * @param procInstId 流程实例ID
     */
    void endProcess(String procInstId);

    /**
     * 删除历史环节信息
     *
     * @param taskId 环节ID
     */
    void removeHiTask(String taskId);

    /**
     * 获取所有任务节点
     *
     * @param procDefKey 流程定义Key，该参数获取最新流程
     */
    Map<String, Object> getUserTasksByProcDefKey(String procDefKey);

    /**
     * 获取所有任务节点
     *
     * @param procInstId 流程实例ID，该参数获取当前流程实例对应流程
     */
    Map<String, Object> getUserTasksByProcInstId(String procInstId);

    /**
     * 设置流程变量
     *
     * @param taskId 当前环节ID
     * @param map    key-value
     */
    void setVariables(String taskId, Map<String, Object> map);

    /**
     * 删除流程变量
     *
     * @param taskId 当前环节ID
     * @param var2   需要删除的 key
     */
    void removeVariables(String taskId, Collection<String> var2);

    /**
     * 设置任务变量
     *
     * @param taskId 当前环节ID
     */
    void setVariablesLocal(String taskId, Map<String, Object> map);

    /***
     * 获取流程变量
     * @param procInstId  流程实例ID
     */
    Map<String, Object> getVariablesByProcInstId(String procInstId);

    /**
     * 获取流程变量
     *
     * @param taskId
     * @return
     */
    Map<String, Object> getVariablesByTaskId(String taskId);
}
