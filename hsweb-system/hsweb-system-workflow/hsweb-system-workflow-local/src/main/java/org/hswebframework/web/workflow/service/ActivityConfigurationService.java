package org.hswebframework.web.workflow.service;

import org.hswebframework.web.workflow.service.dto.ActivityCandidateInfo;

import java.util.List;

public interface ActivityConfigurationService {

    /**
     * 获取一个流程环节的办理候选人
     *
     * @param doingUserId     当前办理用户ID
     * @param processDefineId 流程定义ID
     * @param activityId      环节Id
     * @return 该环节的候选人, 如果没有候选人则返回空集合.不会返回null
     */
    List<ActivityCandidateInfo> getCandidate(String doingUserId, String processDefineId, String activityId);


}
