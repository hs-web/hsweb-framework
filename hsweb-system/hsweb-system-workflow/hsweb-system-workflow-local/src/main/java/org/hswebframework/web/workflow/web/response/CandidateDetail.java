package org.hswebframework.web.workflow.web.response;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.bean.Bean;
import org.hswebframework.web.organizational.authorization.Position;
import org.hswebframework.web.workflow.service.config.CandidateInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
public class CandidateDetail implements Bean {

    private static final long serialVersionUID = 7568237438666348299L;

    private String userId;

    private String personId;

    private String name;

    private List<Position> positions;

    public static CandidateDetail of(CandidateInfo candidateInfo) {
        CandidateDetail detail = new CandidateDetail();

        if (candidateInfo.user() != null) {
            detail.setName(candidateInfo.user().getUser().getName());
            detail.setUserId(candidateInfo.user().getUser().getId());
        }

        if (candidateInfo.person() != null) {
            detail.setPersonId(candidateInfo.person().getPersonnel().getId());
            detail.setName(candidateInfo.person().getPersonnel().getName());
            detail.setPositions(new ArrayList<>(candidateInfo.person().getPositions()));
        }

        return detail;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CandidateDetail) {
            return ((CandidateDetail) obj).getUserId().equals(userId);
        }
        return super.equals(obj);
    }
}
