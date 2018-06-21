package org.hswebframework.web.workflow.dimension;

import org.hswebframework.web.authorization.AuthenticationPredicate;

/**
 * @author zhouhao
 * @see AuthenticationPredicate
 * @since 3.0.0-RC
 */
public interface PermissionDimensionParser {
    AuthenticationPredicate parse(String jsonConfig);
}
