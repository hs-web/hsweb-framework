package org.hswebframework.web.workflow.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
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

      PagerResult<Record> selectProcessForm(String processDefineId, QueryParamEntity queryParam);

      PagerResult<Record> selectTaskForm(String processDefineId, String activityId, QueryParamEntity queryParam);

}
