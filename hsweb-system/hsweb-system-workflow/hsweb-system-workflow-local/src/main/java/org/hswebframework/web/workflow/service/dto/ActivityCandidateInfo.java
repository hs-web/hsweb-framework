package org.hswebframework.web.workflow.service.dto;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;

public interface ActivityCandidateInfo {
    /**
     * 候选人的用户授权信息
     */
    Authentication user();

    /**
     * 候选人的组织架构人员信息
     */
    PersonnelAuthentication person();
}
