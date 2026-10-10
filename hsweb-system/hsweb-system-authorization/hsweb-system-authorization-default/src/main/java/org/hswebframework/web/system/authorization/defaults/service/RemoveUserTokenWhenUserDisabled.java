package org.hswebframework.web.system.authorization.defaults.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.UserModifiedEvent;
import org.hswebframework.web.system.authorization.api.event.UserStateChangedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 按既有用户修改和状态变更事件注销非启用用户的 token。
 * 仅处理 token 生命周期，不限制非禁用用户的内部权限初始化。
 */
@AllArgsConstructor
@Slf4j
public class RemoveUserTokenWhenUserDisabled {

    private final UserTokenManager userTokenManager;

    @EventListener
    public void handleStateChangeEvent(UserModifiedEvent event) {
        if (event.getUserEntity().getStatus() != null && event.getUserEntity().getStatus() != UserEntity.STATUS_ENABLED) {
            event.async(
                    Mono.just(event.getUserEntity().getId())
                        .flatMap(userTokenManager::signOutByUserId)
            );
        }
    }

    @EventListener
    public void handleStateChangeEvent(UserStateChangedEvent event) {
        if (event.getState() != UserEntity.STATUS_ENABLED) {
            event.async(
                    Flux.fromIterable(event.getUserIdList())
                        .flatMap(userTokenManager::signOutByUserId)
            );
        }
    }

}
