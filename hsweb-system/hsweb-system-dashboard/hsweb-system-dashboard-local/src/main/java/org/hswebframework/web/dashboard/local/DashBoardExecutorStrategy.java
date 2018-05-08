package org.hswebframework.web.dashboard.local;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;

public interface DashBoardExecutorStrategy {

    boolean support(DashBoardConfigEntity entity);

    Object execute(DashBoardConfigEntity entity, Authentication authentication);
}
