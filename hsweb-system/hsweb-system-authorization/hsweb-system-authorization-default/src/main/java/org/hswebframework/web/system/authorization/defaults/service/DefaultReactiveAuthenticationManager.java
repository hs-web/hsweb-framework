package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.web.authorization.*;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import reactor.core.publisher.Mono;

public class DefaultReactiveAuthenticationManager implements ReactiveAuthenticationManagerProvider {

    @Autowired
    private ReactiveUserService reactiveUserService;

    @Autowired
    private ReactiveAuthenticationInitializeService initializeService;

    @Autowired(required = false)
    private ReactiveCacheManager cacheManager;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return request
                .filter(PlainTextUsernamePasswordAuthenticationRequest.class::isInstance)
                .switchIfEmpty(Mono.error(() -> new UnsupportedOperationException("不支持的请求类型")))
                .map(PlainTextUsernamePasswordAuthenticationRequest.class::cast)
                .flatMap(pwdRequest -> reactiveUserService.findByUsernameAndPassword(pwdRequest.getUsername(), pwdRequest.getPassword()))
                .switchIfEmpty(Mono.error(() -> new AccessDenyException("密码错误")))
                .map(UserEntity::getId)
                .flatMap(this::getByUserId);
    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {

        return Mono.justOrEmpty(userId)
                .flatMap(_id -> Mono.justOrEmpty(cacheManager)
                        .map(cm -> cacheManager.<Authentication>getCache("user-auth"))
                        .flatMap(cache -> cache.mono(userId).onCacheMissResume(() -> initializeService.initUserAuthorization(userId)))
                        .cast(Authentication.class)
                        .switchIfEmpty(initializeService.initUserAuthorization(userId)));
    }
}
