package org.hswebframework.web.workflow.dimension;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface CandidateDimension {

    List<String> getCandidateUserIdList();


    CandidateDimension empty =  Collections::emptyList;

}
