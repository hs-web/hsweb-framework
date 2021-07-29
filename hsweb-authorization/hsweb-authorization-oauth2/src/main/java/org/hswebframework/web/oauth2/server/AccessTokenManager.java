package org.hswebframework.web.oauth2.server;

import org.hswebframework.web.authorization.Authentication;
import reactor.core.publisher.Mono;

/**
 * OAuth2 AccessToken管理器,用于创建,刷新token以及根据token获取权限信息
 *
 * @author zhouhao
 * @since 4.0.7
 */
public interface AccessTokenManager {

    /**
     * 根据token获取权限信息
     *
     * @param accessToken accessToken
     * @return 权限信息
     */
    Mono<Authentication> getAuthenticationByToken(String accessToken);

    /**
     * 根据ClientId以及权限信息创建token
     *
     * @param clientId       clientId {@link OAuth2Client#getClientId()}
     * @param authentication 权限信息
     * @param singleton      是否单例,如果为true,重复创建token将返回首次创建的token
     * @return
     */
    Mono<AccessToken> createAccessToken(String clientId,
                                        Authentication authentication,
                                        boolean singleton);

    /**
     * 刷新token
     *
     * @param clientId     clientId {@link OAuth2Client#getClientId()}
     * @param refreshToken refreshToken
     * @return 新的token
     */
    Mono<AccessToken> refreshAccessToken(String clientId, String refreshToken);

}
