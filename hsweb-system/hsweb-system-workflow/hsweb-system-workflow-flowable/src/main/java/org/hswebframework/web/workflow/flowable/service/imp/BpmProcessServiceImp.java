package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.workflow.flowable.service.BpmProcessService;
import org.hswebframework.web.workflow.flowable.service.BpmTaskService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class BpmProcessServiceImp extends FlowableAbstract implements BpmProcessService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
   private BpmTaskService bpmTaskService;

    @Override
    public List<ProcessDefinition> getAllProcessDefinition() {
        return repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
    }

    @Override
    public ProcessInstance startProcessInstance(String creatorId,
                                                String procDefKey,
                                                String activity,
                                                String nextClaim,
                                                String businessKey,
                                                Map<String, Object> variables){
        logger.debug("start flow :", procDefKey);
        ProcessInstance processInstance = null;
        try{
            // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
            identityService.setAuthenticatedUserId(creatorId);
            logger.debug("流程启动,work_flow_key:{}", procDefKey);
            logger.debug("表单ID,businessKey:{}",businessKey);
            logger.debug("流程变量保存,variables:{}",variables);
            //启动流程，返回流程实例
            processInstance = runtimeService.startProcessInstanceByKey(procDefKey, businessKey, variables);
            //获取流程实例ID
            String processInstanceId = processInstance.getId();
            logger.debug("流程启动成功,流程ID:{}",processInstanceId);
            List<Task> tasks = bpmTaskService.selectTaskByProcessId(processInstanceId);
            //如果指定了下一步执行环节，则将流程跳转到指定环节,并删除当前未执行的环节历史信息
            if(!StringUtils.isNullOrEmpty(activity)){
                bpmTaskService.jumpTask(processInstanceId,activity,StringUtils.isNullOrEmpty(nextClaim)?"":nextClaim);
                for(Task task:tasks){
                    bpmTaskService.removeHiTask(task.getId());
                }
            }else{
                // 设置待办人（单环节并且设定办理人可直接签收）
                if(tasks.size()==1 && !StringUtils.isNullOrEmpty(nextClaim)) bpmTaskService.claim(tasks.get(0).getId(), nextClaim);
                else {
                    for(Task task:tasks){
                        bpmTaskService.addCandidateUser(task.getId(), task.getTaskDefinitionKey(), creatorId);
                    }
                }
            }

            if (logger.isDebugEnabled())
                logger.debug("start process of {key={}, bkey={}, pid={}, variables={}}", procDefKey, businessKey, processInstanceId, variables);
        }catch (Exception e){
            logger.warn("工作流启动失败，请联系管理员!");
        }finally {
            identityService.setAuthenticatedUserId(null);
        }
        return processInstance;
    }

    @Override
    public List<ProcessInstance> getProcessInstances(int page, int num, String procDefKey) {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(procDefKey).listPage(page, num);
    }

    @Override
    public void closeProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void openProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId){
        return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    }

    @Override
    public ProcessDefinition getProcessDefinitionByKey(String procDefKey) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).orderByProcessDefinitionVersion().desc().list().get(0);
    }

    @Override
    public InputStream findProcessPic(String procDefId) {
        ProcessDefinition definition = getProcessDefinitionById(procDefId);
        String source = definition.getDiagramResourceName();
        return repositoryService.getResourceAsStream(definition.getDeploymentId(),source);
    }

    @Override
    public Job getJob(String processInstanceId) {
        return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public int deleteJob(String jobId) {
        return 0;
    }

}
