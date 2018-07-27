package org.hswebframework.web.workflow.listener;

import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ProcessEvent {

      ProcessInstance getProcessInstance();

}
