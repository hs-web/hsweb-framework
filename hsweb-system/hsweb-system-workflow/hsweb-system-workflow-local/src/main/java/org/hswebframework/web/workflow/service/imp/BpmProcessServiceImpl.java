package org.hswebframework.web.workflow.service.imp;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.workflow.dao.entity.ProcessHistoryEntity;
import org.hswebframework.web.workflow.service.BpmProcessService;
import org.hswebframework.web.workflow.service.BpmTaskService;
import org.hswebframework.web.workflow.service.ProcessHistoryService;
import org.hswebframework.web.workflow.service.WorkFlowFormService;
import org.hswebframework.web.workflow.service.request.SaveFormRequest;
import org.hswebframework.web.workflow.service.request.StartProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author wangwei
 * @Date 2017/8/7.
 */
@Service
@Transactional(rollbackFor = Throwable.class)
public class BpmProcessServiceImpl extends AbstractFlowableService implements BpmProcessService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BpmTaskService bpmTaskService;

    @Autowired
    private WorkFlowFormService workFlowFormService;

    @Autowired
    private ProcessHistoryService processHistoryService;

    @Override
    public List<ProcessDefinition> getAllProcessDefinition() {
        return repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
    }

    @Override
    public ProcessInstance startProcessInstance(StartProcessRequest request) {
        request.tryValidate();
        ProcessInstance processInstance;
        logger.debug("start workflow :{}", request);
        try {
            identityService.setAuthenticatedUserId(request.getCreatorId());

            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(request.getProcessDefineId())
                    .singleResult();
            if (definition == null) {
                throw new NotFoundException("流程[" + request.getProcessDefineId() + "]不存在");
            }

            //创建业务ID
            String businessKey = IDGenerator.MD5.generate();

            //启动流程
            processInstance = runtimeService.startProcessInstanceById(
                    request.getProcessDefineId()
                    , businessKey
                    , request.getVariables());

            //候选人设置
            Consumer<Task> candidateUserSetter = (task) -> {
                if (task == null) {
                    return;
                }
                //指定了下一环节的办理人
                if (!StringUtils.isNullOrEmpty(request.getNextClaimUserId())) {
                    taskService.addCandidateUser(task.getId(), request.getNextClaimUserId());
                } else {
                    bpmTaskService.setCandidate(request.getCreatorId(), task);
                }
            };

            List<Task> tasks = bpmTaskService.selectTaskByProcessId(processInstance.getProcessDefinitionId());

            //当前节点
            String activityId = processInstance.getActivityId();
            if (activityId == null) {
                //所有task设置候选人
                tasks.forEach(candidateUserSetter);
            } else {
                candidateUserSetter.accept(taskService
                        .createTaskQuery()
                        .processInstanceId(processInstance.getProcessInstanceId())
                        .taskDefinitionKey(activityId)
                        .active()
                        .singleResult());
            }

            workFlowFormService.saveProcessForm(processInstance, SaveFormRequest
                    .builder()
                    .userId(request.getCreatorId())
                    .userName(request.getCreatorName())
                    .formData(request.getFormData())
                    .build());

            ProcessHistoryEntity history = ProcessHistoryEntity.builder()
                    .type("start")
                    .typeText("启动流程")
                    .businessKey(businessKey)
                    .creatorId(request.getCreatorId())
                    .creatorName(request.getCreatorName())
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .processDefineId(processInstance.getProcessDefinitionId())
                    .build();

            processHistoryService.insert(history);


        } finally {
            identityService.setAuthenticatedUserId(null);
        }
        return processInstance;
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
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
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
        return repositoryService.getResourceAsStream(definition.getDeploymentId(), source);
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
