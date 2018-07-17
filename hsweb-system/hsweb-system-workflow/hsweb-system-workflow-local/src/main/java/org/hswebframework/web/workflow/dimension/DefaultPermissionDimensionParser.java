package org.hswebframework.web.workflow.dimension;

import org.hswebframework.web.authorization.AuthenticationPredicate;
import org.hswebframework.web.authorization.AuthenticationUtils;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
public class DefaultPermissionDimensionParser implements PermissionDimensionParser {
    @Override
    public AuthenticationPredicate parse(String jsonConfig) {

        return AuthenticationUtils.createPredicate(jsonConfig);
    }
}
