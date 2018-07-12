package org.hswebframework.web.workflow.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.web.workflow.service.request.SaveFormRequest;


/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface WorkFlowFormService {
    void saveProcessForm(ProcessInstance instance, SaveFormRequest request);

    void saveTaskForm(Task task,SaveFormRequest request);
}
