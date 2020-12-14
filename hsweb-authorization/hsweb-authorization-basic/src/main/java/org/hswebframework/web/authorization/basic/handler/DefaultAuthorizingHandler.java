package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.HandleType;
import org.hswebframework.web.authorization.define.ResourcesDefinition;
import org.hswebframework.web.authorization.events.AuthorizingHandleBeforeEvent;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author zhouhao
 */
public class DefaultAuthorizingHandler implements AuthorizingHandler {

    private DataAccessController dataAccessController;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationEventPublisher eventPublisher;

    public DefaultAuthorizingHandler(DataAccessController dataAccessController) {
        this.dataAccessController = dataAccessController;
    }

    public DefaultAuthorizingHandler() {
    }

    public void setDataAccessController(DataAccessController dataAccessController) {
        this.dataAccessController = dataAccessController;
    }

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handRBAC(AuthorizingContext context) {
        if (handleEvent(context, HandleType.RBAC)) {
            return;
        }
        //进行rdac权限控制
        handleRBAC(context.getAuthentication(), context.getDefinition());

    }

    private boolean handleEvent(AuthorizingContext context, HandleType type) {
        if (null != eventPublisher) {
            AuthorizingHandleBeforeEvent event = new AuthorizingHandleBeforeEvent(context, type);
            eventPublisher.publishEvent(event);
            if (!event.isExecute()) {
                if (event.isAllow()) {
                    return true;
                } else {
                    throw new AccessDenyException(event.getMessage());
                }
            }
        }
        return false;
    }

    public void handleDataAccess(AuthorizingContext context) {

        if (dataAccessController == null) {
            logger.warn("dataAccessController is null,skip result access control!");
            return;
        }
        if (context.getDefinition().getResources() == null) {
            return;
        }
        if (handleEvent(context, HandleType.DATA)) {
            return;
        }

        DataAccessController finalAccessController = dataAccessController;
        Authentication autz = context.getAuthentication();

        boolean isAccess = context.getDefinition()
                .getResources()
                .getDataAccessResources()
                .stream()
                .allMatch(resource -> {
                    Permission permission = autz.getPermission(resource.getId()).orElseThrow(AccessDenyException::new);
                    return resource.getDataAccessAction()
                            .stream()
                            .allMatch(act -> permission.getDataAccesses(act.getId())
                                    .stream()
                                    .allMatch(dataAccessConfig -> finalAccessController.doAccess(dataAccessConfig, context)));

                });
        if (!isAccess) {
            throw new AccessDenyException(context.getDefinition().getMessage());
        }
    }


    protected void handleRBAC(Authentication authentication, AuthorizeDefinition definition) {

        ResourcesDefinition resources = definition.getResources();

        if (!resources.hasPermission(authentication.getPermissions())) {
            throw new AccessDenyException(definition.getMessage(),definition.getDescription());
        }
    }
}
