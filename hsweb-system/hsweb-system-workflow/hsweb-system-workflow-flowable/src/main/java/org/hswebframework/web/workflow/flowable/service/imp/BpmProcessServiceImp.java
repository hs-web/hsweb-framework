package org.hswebframework.web.workflow.flowable.service.imp;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.workflow.flowable.service.BpmProcessService;
import org.hswebframework.web.workflow.flowable.service.BpmTaskService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
public class BpmProcessServiceImp extends FlowableAbstract implements BpmProcessService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    BpmTaskService bpmTaskService;

    @Override
    public ProcessInstance startProcessInstance(String creator_id,String procDefKey,String activity,String next_claim,
                                String businessKey,
                                Map<String, Object> variables){
        logger.debug("start flow :", procDefKey);
        ProcessInstance processInstance = null;
        try{
            // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
            identityService.setAuthenticatedUserId(creator_id);
            logger.debug("流程启动,work_flow_key:{}", procDefKey);
            logger.debug("表单ID,businessKey:{}",businessKey);
            logger.debug("流程变量保存,variables:{}",variables);
            //启动流程，返回流程实例
            processInstance = runtimeService.startProcessInstanceByKey(procDefKey, businessKey, variables);
            //获取流程实例ID
            String processInstanceId = processInstance.getId();
            logger.debug("流程启动成功,流程ID:{}",processInstanceId);
            Task task = bpmTaskService.selectTaskByProcessId(processInstanceId);
            //如果指定了下一步执行环节，则将流程跳转到指定环节,并删除当前未执行的环节历史信息
            if(!StringUtils.isNullOrEmpty(activity)){
                bpmTaskService.jumpTask(processInstanceId,activity,StringUtils.isNullOrEmpty(next_claim)?"":next_claim);
                bpmTaskService.removeHiTask(task.getId());
            }else{
                //流程签收，签收人为指定办理人
                if(!StringUtils.isNullOrEmpty(next_claim))
                    bpmTaskService.claim(task.getId(), next_claim);
            }

            if (logger.isDebugEnabled())
                logger.debug("start process of {key={}, bkey={}, pid={}, variables={}}", new Object[]{procDefKey, businessKey, processInstanceId, variables});
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
        InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(),source);
        return inputStream;
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
