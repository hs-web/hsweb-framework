package org.hswebframework.web.authorization.basic.handler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.define.*;
import org.hswebframework.web.authorization.events.AuthorizingHandleBeforeEvent;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 */
@Slf4j
public class DefaultAuthorizingHandler implements AuthorizingHandler {

    private DataAccessController dataAccessController;

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

    @Override
    public Mono<Void> handRBACAsync(AuthorizingContext context) {
        return this
            .handleEventAsync(context, HandleType.RBAC)
            .doOnNext(handled -> {
                //没有自定义事件处理
                if (!handled) {
                    handleRBAC(context.getAuthentication(), context.getDefinition());
                }
            })
            .then();
    }

    private Mono<Boolean> handleEventAsync(AuthorizingContext context, HandleType type) {
        if (null != eventPublisher) {
            AuthorizingHandleBeforeEvent event = new AuthorizingHandleBeforeEvent(context, type);
            return event
                .publish(eventPublisher)
                .then(Mono.fromCallable(() -> {
                    if (!event.isExecute()) {
                        if (event.isAllow()) {
                            return true;
                        } else {
                            throw new AccessDenyException.NoStackTrace(event.getMessage());
                        }
                    }
                    return false;
                }));
        }
        return Mono.just(false);
    }

    @SneakyThrows
    private boolean handleEvent(AuthorizingContext context, HandleType type) {
        if (null != eventPublisher) {
            AuthorizingHandleBeforeEvent event = new AuthorizingHandleBeforeEvent(context, type);
            eventPublisher.publishEvent(event);
            if (event.hasListener()) {
                event.getAsync().block();
            }
            if (!event.isExecute()) {
                if (event.isAllow()) {
                    return true;
                } else {
                    throw new AccessDenyException.NoStackTrace(event.getMessage());
                }
            }
        }
        return false;
    }

    @Deprecated
    public void handleDataAccess(AuthorizingContext context) {


    }


    protected void handleRBAC(Authentication authentication, AuthorizeDefinition definition) {

        ResourcesDefinition resources = definition.getResources();
        // 判断权限
        if (!resources.hasPermission(authentication)) {
            throw new AccessDenyException.NoStackTrace(definition.getMessage(), definition.getDescription());
        }

        DimensionsDefinition dd = definition.getDimensions();
        // 判断维度
        if (dd != null && !dd.isEmpty()) {
            if (!dd.hasDimension(
                (type, logical, dimensionIds) ->
                    hasDimensions(authentication, type, logical, dimensionIds))) {

                throw new AccessDenyException
                    .NoStackTrace(definition.getMessage(), definition.getDimensions().toString());

            }
        }
    }

    private boolean hasDimensions(Authentication auth, String type, Logical logical, Set<String> dimensionIds) {
        if (logical == Logical.AND) {
            return auth.hasAllDimension(type, dimensionIds);
        }
        return auth.hasAnyDimension(type, dimensionIds);
    }
}
