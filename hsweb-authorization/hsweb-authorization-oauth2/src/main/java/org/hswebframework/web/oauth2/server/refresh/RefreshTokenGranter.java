package org.hswebframework.web.oauth2.server.refresh;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialRequest;
import reactor.core.publisher.Mono;

public interface RefreshTokenGranter {

    /**
     * 刷新token
     *
     * @param request 请求
     * @return token
     */
    Mono<AccessToken> requestToken(RefreshTokenRequest request);


}
