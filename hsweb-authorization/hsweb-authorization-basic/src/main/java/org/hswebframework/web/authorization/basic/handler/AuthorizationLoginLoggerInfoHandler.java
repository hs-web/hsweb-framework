package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.events.AuthorizationSuccessEvent;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;

/**
 * @author gyl
 * @since 2.2
 */
public class AuthorizationLoginLoggerInfoHandler {

    @EventListener
    public void fillLoggerInfoAuth(AuthorizationSuccessEvent event) {
        event.async(
                //填充操作日志用户认证信息
                Mono.deferContextual(ctx -> {
                    ctx.<AccessLoggerInfo>getOrEmpty(AccessLoggerInfo.class)
                       .ifPresent(loggerInfo -> {
                           Authentication auth = event.getAuthentication();
                           loggerInfo.putContext("userId", auth.getUser().getId());
                           loggerInfo.putContext("username", auth.getUser().getUsername());
                           loggerInfo.putContext("userName", auth.getUser().getName());
                       });
                    // FIXME: 2024/3/26 未传递用户维度信息,如有需要也可通过上下文传递
                    return Mono.empty();
                })
        );

    }
}
