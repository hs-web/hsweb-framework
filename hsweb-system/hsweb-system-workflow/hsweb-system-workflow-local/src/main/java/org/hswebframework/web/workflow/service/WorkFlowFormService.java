package org.hswebframework.web.workflow.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.workflow.service.request.SaveFormRequest;

import java.util.List;


/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface WorkFlowFormService {
    void saveProcessForm(ProcessInstance instance, SaveFormRequest request);

    void saveTaskForm(ProcessInstance instance,Task task, SaveFormRequest request);

    <T> PagerResult<T> selectProcessForm(String processDefineId, QueryParamEntity queryParam);

    <T> PagerResult<T> selectTaskForm(String processDefineId, String activityId, QueryParamEntity queryParam);

}
