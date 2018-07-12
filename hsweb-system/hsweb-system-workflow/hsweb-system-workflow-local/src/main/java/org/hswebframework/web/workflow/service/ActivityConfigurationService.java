package org.hswebframework.web.workflow.service;

import org.hswebframework.web.workflow.service.dto.ActivityConfiguration;
import org.hswebframework.web.workflow.service.dto.ProcessConfiguration;

public interface ActivityConfigurationService {

    ActivityConfiguration getActivityConfiguration(String doingUser, String processDefineId, String activityId);

    ProcessConfiguration getProcessConfiguration(String processDefineId);

}
