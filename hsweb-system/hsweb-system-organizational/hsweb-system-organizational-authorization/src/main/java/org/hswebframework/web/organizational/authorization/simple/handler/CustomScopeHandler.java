package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.simple.SimpleCustomScopeDataAccessConfig;
import org.hswebframework.web.organizational.authorization.simple.SimpleScopeDataAccessConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author zhouhao
 */
public class CustomScopeHandler implements DataAccessHandler {

    private List<DataAccessHandler> handlers = Arrays.asList(
            new DistrictScopeDataAccessHandler(),
            new DepartmentScopeDataAccessHandler(),
            new OrgScopeDataAccessHandler(),
            new PersonScopeDataAccessHandler(),
            new PositionScopeDataAccessHandler()
    );

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof SimpleCustomScopeDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        return ((SimpleCustomScopeDataAccessConfig) access).getScope()
                .stream()
                .map(scope -> new SimpleScopeDataAccessConfig(scope.getType(), DataAccessType.SCOPE_TYPE_CUSTOM, access.getAction(), new HashSet<>(scope.getIds())))
                .allMatch(accessConfig -> handlers.stream()
                        .filter(handler -> handler.isSupport(accessConfig))
                        .allMatch(handler -> handler.handle(accessConfig, context)));
    }
}
