package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * token 权限管理器,根据token来进行权限关联.
 *
 * @author zhouhao
 * @since 4.0.7
 */
public interface TokenAuthenticationManager {

    /**
     * 根据token获取认证信息
     *
     * @param token token
     * @return 认证信息
     */
    Mono<Authentication> getByToken(String token);

    /**
     * 设置token认证信息
     *
     * @param token token
     * @param auth  认证信息
     * @param ttl   有效期
     * @return void
     */
    Mono<Void> putAuthentication(String token, Authentication auth, Duration ttl);

    /**
     * 删除token
     * @param token token
     * @return void
     */
    Mono<Void> removeToken(String token);
}
