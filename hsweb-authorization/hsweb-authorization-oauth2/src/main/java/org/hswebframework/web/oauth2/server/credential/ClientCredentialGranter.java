package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Granter;
import reactor.core.publisher.Mono;

public interface ClientCredentialGranter extends OAuth2Granter {

    /**
     * 申请token
     *
     * @param request 请求
     * @return token
     */
    Mono<AccessToken> requestToken(ClientCredentialRequest request);


}
