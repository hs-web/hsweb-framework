package org.hswebframework.web.workflow.listener;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface TaskEvent {
    Task getTask();

    ProcessInstance getProcessInstance();
}
