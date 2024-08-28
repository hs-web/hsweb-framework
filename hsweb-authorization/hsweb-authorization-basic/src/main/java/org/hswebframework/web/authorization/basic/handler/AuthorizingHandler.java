package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.define.AuthorizingContext;
import reactor.core.publisher.Mono;

/**
 * aop方式权限控制处理器
 *
 * @author zhouhao
 */
public interface AuthorizingHandler {

    void handRBAC(AuthorizingContext context);

    default Mono<Void> handRBACAsync(AuthorizingContext context) {
        return Mono.fromRunnable(() -> handRBAC(context));
    }

    @Deprecated
    void handleDataAccess(AuthorizingContext context);

    @Deprecated
    default void handle(AuthorizingContext context) {
        handRBAC(context);
        handleDataAccess(context);
    }
}
