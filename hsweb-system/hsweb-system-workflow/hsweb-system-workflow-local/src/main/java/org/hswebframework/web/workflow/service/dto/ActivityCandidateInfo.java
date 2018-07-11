package org.hswebframework.web.workflow.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;

@Getter
@Setter
public class ActivityCandidateInfo {
    private Authentication user;

    private PersonnelAuthentication person;
}
