package org.hswebframework.web.oauth2.server.code;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Granter;
import reactor.core.publisher.Mono;

/**
 * 授权码模式认证
 *
 * @author zhouhao
 * @since 4.0.7
 */
public interface AuthorizationCodeGranter extends OAuth2Granter {

    /**
     * @return 申请授权码界面
     */
    String getLoginUrl();

    /**
     * 申请授权码
     *
     * @param request 请求
     * @return 授权码信息
     */
    Mono<AuthorizationCodeResponse> requestCode(AuthorizationCodeRequest request);

    /**
     * 根据授权码获取token
     *
     * @param request 请求
     * @return token
     */
    Mono<AccessToken> requestToken(AuthorizationCodeTokenRequest request);

}
