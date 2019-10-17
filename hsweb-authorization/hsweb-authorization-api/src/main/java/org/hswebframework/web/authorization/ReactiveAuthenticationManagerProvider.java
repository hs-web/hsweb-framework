package org.hswebframework.web.authorization;

import reactor.core.publisher.Mono;

public interface ReactiveAuthenticationManagerProvider {
    /**
     * 进行授权操作
     *
     * @param request 授权请求
     * @return 授权成功则返回用户权限信息
     */
    Mono<Authentication> authenticate(Mono<AuthenticationRequest> request);

    /**
     * 根据用户ID获取权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    Mono<Authentication> getByUserId(String userId);
}
