package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.hswebframework.web.authorization.exception.AuthenticationException;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@Slf4j
@Order(100)
public class DefaultReactiveAuthenticationManager implements ReactiveAuthenticationManagerProvider {

    @Autowired
    private ReactiveUserService reactiveUserService;

    @Autowired
    private ReactiveAuthenticationInitializeService initializeService;

    @Autowired(required = false)
    private ReactiveCacheManager cacheManager;

    @EventListener
    public void handleClearAuthCache(ClearUserAuthorizationCacheEvent event) {
        if (cacheManager != null) {
            Mono<Void> operator;
            if (event.isAll()) {
                operator = cacheManager
                        .getCache("user-auth")
                        .clear()
                        .doOnSuccess(nil -> log.info("clear all user authentication cache success"))
                        .doOnError(err -> log.error(err.getMessage(), err));
            } else {
                operator = cacheManager
                        .getCache("user-auth")
                        .evictAll(event.getUserId())
                        .doOnError(err -> log.error(err.getMessage(), err))
                        .doOnSuccess(__ -> log.info("clear user {} authentication cache success", event.getUserId()));
            }
            if (event.isAsync()) {
                event.first(operator);
            } else {
                log.warn("please use async for ClearUserAuthorizationCacheEvent");
                operator.subscribe();
            }
        }
    }

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return request
                .filter(PlainTextUsernamePasswordAuthenticationRequest.class::isInstance)
                .switchIfEmpty(Mono.error(() -> new UnsupportedOperationException("不支持的请求类型")))
                .map(PlainTextUsernamePasswordAuthenticationRequest.class::cast)
                .flatMap(pwdRequest -> reactiveUserService
                    .findByUsernameAndPassword(pwdRequest.getUsername(), pwdRequest.getPassword())
                    .flatMap(user -> {
                        if (Byte.valueOf((byte) 1).equals(user.getStatus())) {
                            return Mono.just(user.getId());
                        }
                        return Mono.error(() -> new AuthenticationException(AuthenticationException.USER_DISABLED));
                    })
                )
                .flatMap(this::getByUserId);
    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        if (userId == null) {
            return Mono.empty();
        }
        if (cacheManager == null) {
            return initializeService.initUserAuthorization(userId);
        }

        return cacheManager
                .<Authentication>getCache("user-auth")
                .getMono(userId, () -> initializeService.initUserAuthorization(userId));
    }
}
