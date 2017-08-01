package org.hswebframework.web.workflow.flowable.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.*;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2017/7/26.
 */
public abstract class FlowableAbstract {
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
    @Resource
    protected ObjectMapper objectMapper;
}
