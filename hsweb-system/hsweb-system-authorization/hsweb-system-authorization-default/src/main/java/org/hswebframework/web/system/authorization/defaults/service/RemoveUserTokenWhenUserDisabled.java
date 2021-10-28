package org.hswebframework.web.system.authorization.defaults.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.system.authorization.api.event.UserModifiedEvent;
import org.hswebframework.web.system.authorization.api.event.UserStateChangedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class RemoveUserTokenWhenUserDisabled {

    private final UserTokenManager userTokenManager;

    @EventListener
    public void handleStateChangeEvent(UserModifiedEvent event) {
        if (event.getUserEntity().getStatus() != null && event.getUserEntity().getStatus() != 1) {
            event.async(
                    Mono.just(event.getUserEntity().getId())
                        .flatMap(userTokenManager::signOutByUserId)
            );
        }
    }

    @EventListener
    public void handleStateChangeEvent(UserStateChangedEvent event) {
        if (event.getState() != 1) {
            event.async(
                    Flux.fromIterable(event.getUserIdList())
                        .flatMap(userTokenManager::signOutByUserId)
            );
        }
    }

}
