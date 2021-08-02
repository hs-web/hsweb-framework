package org.hswebframework.web.oauth2.server.refresh;

import lombok.AllArgsConstructor;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class DefaultRefreshTokenGranter implements RefreshTokenGranter {

    private final AccessTokenManager accessTokenManager;

    @Override
    public Mono<AccessToken> requestToken(RefreshTokenRequest request) {

        return accessTokenManager
                .refreshAccessToken(
                        request.getClient().getClientId(),
                        request.refreshToken().orElseThrow(()->new OAuth2Exception(ErrorType.ILLEGAL_REFRESH_TOKEN))
                        );
    }
}
