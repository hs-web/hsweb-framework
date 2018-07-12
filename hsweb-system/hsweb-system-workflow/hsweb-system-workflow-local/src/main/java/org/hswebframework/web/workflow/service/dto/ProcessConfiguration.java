package org.hswebframework.web.workflow.service.dto;


import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ProcessConfiguration {
    String getFormId();

    void assertCanStartProcess(String userId, ProcessDefinition definition);
}
