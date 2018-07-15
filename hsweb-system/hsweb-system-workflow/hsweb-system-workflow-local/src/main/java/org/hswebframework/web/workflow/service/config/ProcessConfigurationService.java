package org.hswebframework.web.workflow.service.config;


public interface ProcessConfigurationService {

    ActivityConfiguration getActivityConfiguration(String doingUser, String processDefineId, String activityId);

    ProcessConfiguration getProcessConfiguration(String processDefineId);

}
