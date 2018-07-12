package org.hswebframework.web.workflow.service.dto;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ActivityConfiguration {
    String getFormId();

    boolean canClaim(String userId);

    List<ActivityCandidateInfo> getCandidateInfo();

}
