package org.hswebframework.web.workflow.service.config;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;

/**
 * 候选人信息
 */
public interface CandidateInfo {
    /**
     * 候选人的用户授权信息
     *
     * @see Authentication
     */
    Authentication user();

    /**
     * 候选人的组织架构人员信息
     *
     * @see PersonnelAuthentication
     */
    PersonnelAuthentication person();
}
