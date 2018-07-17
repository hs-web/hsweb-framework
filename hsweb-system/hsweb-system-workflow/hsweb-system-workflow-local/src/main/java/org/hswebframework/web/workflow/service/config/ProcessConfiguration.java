package org.hswebframework.web.workflow.service.config;


import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ProcessConfiguration {
    String getFormId();

    void assertCanStartProcess(String userId, ProcessDefinition definition);

    boolean canStartProcess(String userId, ProcessDefinition definition);
}
