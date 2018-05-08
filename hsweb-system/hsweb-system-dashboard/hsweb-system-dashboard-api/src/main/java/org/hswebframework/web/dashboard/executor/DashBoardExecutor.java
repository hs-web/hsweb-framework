package org.hswebframework.web.dashboard.executor;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;

public interface DashBoardExecutor {
    Object execute(DashBoardConfigEntity entity, Authentication authentication);
}
