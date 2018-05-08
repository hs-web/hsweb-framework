package org.hswebframework.web.dashboard.local;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;
import org.hswebframework.web.dashboard.executor.DashBoardExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class DefaultDashBordExecutor implements DashBoardExecutor {

    @Autowired
    private List<DashBoardExecutorStrategy> strategies;

    @Override
    public Object execute(DashBoardConfigEntity entity, Authentication authentication) {

        Assert.notNull(entity, "配置不能为空");

        return strategies.stream()
                .filter(strategy -> strategy.support(entity))
                .findFirst()
                .map(strategy -> strategy.execute(entity, authentication))
                .orElse(null);
    }
}
