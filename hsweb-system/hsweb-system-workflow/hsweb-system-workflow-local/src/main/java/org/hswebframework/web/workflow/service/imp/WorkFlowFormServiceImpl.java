package org.hswebframework.web.workflow.service.imp;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.workflow.service.ActivityConfigurationService;
import org.hswebframework.web.workflow.service.WorkFlowFormService;
import org.hswebframework.web.workflow.service.dto.ActivityConfiguration;
import org.hswebframework.web.workflow.service.dto.ProcessConfiguration;
import org.hswebframework.web.workflow.service.request.SaveFormRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Service
public class WorkFlowFormServiceImpl extends AbstractFlowableService implements WorkFlowFormService {

    @Autowired
    private ActivityConfigurationService activityConfigurationService;

    @Autowired
    private DynamicFormOperationService dynamicFormOperationService;

    @Override
    public void saveProcessForm(ProcessInstance instance, SaveFormRequest request) {
        request.tryValidate();

        ProcessConfiguration configuration = activityConfigurationService
                .getProcessConfiguration(instance.getProcessDefinitionId());

        if (configuration == null || StringUtils.isEmpty(configuration.getFormId())) {
            return;
        }
        Map<String, Object> formData = request.getFormData();

        acceptStartProcessFormData(instance, formData);

        dynamicFormOperationService.saveOrUpdate(configuration.getFormId(), formData);

    }

    @Override
    public void saveTaskForm(Task task, SaveFormRequest request) {
        request.tryValidate();

        ActivityConfiguration configuration = activityConfigurationService
                .getActivityConfiguration(request.getUserId()
                        , task.getProcessDefinitionId()
                        , task.getTaskDefinitionKey());

        if (configuration == null || StringUtils.isEmpty(configuration.getFormId())) {
            return;
        }

        Map<String, Object> formData = request.getFormData();

        acceptTaskFormData(task, formData);

        dynamicFormOperationService.saveOrUpdate(configuration.getFormId(), formData);

    }

    protected void acceptTaskFormData(Task task,
                                      Map<String, Object> formData) {

        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        acceptStartProcessFormData(instance, formData);

        formData.put("taskId", task.getId());
        formData.put("taskDefineKey", task.getTaskDefinitionKey());

    }

    protected void acceptStartProcessFormData(ProcessInstance instance,
                                              Map<String, Object> formData) {

        formData.put("id", instance.getBusinessKey());
        formData.put("processDefineId", instance.getProcessDefinitionId());
        formData.put("processDefineKey", instance.getProcessDefinitionKey());
        formData.put("processDefineName", instance.getProcessDefinitionName());
        formData.put("processDefineName", instance.getProcessDefinitionVersion());
        formData.put("processInstanceId", instance.getProcessInstanceId());

    }
}
