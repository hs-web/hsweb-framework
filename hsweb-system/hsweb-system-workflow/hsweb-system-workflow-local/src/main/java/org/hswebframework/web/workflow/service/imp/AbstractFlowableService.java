package org.hswebframework.web.workflow.service.imp;

import org.activiti.engine.*;

import javax.annotation.Resource;

/**
 * @author wangwei
 * @author zhouhao
 */
public abstract class AbstractFlowableService {
    @Resource
    protected ProcessEngine processEngine;
    @Resource
    protected RepositoryService repositoryService;
    @Resource
    protected RuntimeService runtimeService;
    @Resource
    protected TaskService taskService;
    @Resource
    protected HistoryService historyService;
    @Resource
    protected IdentityService identityService;
    @Resource
    protected ManagementService managementService;
    @Resource
    protected FormService bpmFormService;
}
