package org.hswebframework.web.oauth2.server;

import org.hswebframework.web.authorization.Authentication;
import reactor.core.publisher.Mono;

public interface AccessTokenManager {

    Mono<Authentication> getAuthenticationByToken(String accessToken);

    Mono<AccessToken> createAccessToken(String clientId,
                                        Authentication authentication,
                                        boolean singleton);

    Mono<AccessToken> refreshAccessToken(String clientId, String refreshToken);

}
