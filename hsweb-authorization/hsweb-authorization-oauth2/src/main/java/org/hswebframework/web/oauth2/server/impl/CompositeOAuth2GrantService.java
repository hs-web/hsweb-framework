package org.hswebframework.web.oauth2.server.impl;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.OAuth2GrantService;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenGranter;

@Getter
@Setter
public class CompositeOAuth2GrantService implements OAuth2GrantService {

    private AuthorizationCodeGranter authorizationCodeGranter;

    private ClientCredentialGranter clientCredentialGranter;

    private RefreshTokenGranter refreshTokenGranter;

    @Override
    public AuthorizationCodeGranter authorizationCode() {
        return authorizationCodeGranter;
    }

    @Override
    public ClientCredentialGranter clientCredential() {
        return clientCredentialGranter;
    }

    @Override
    public RefreshTokenGranter refreshToken() {
        return refreshTokenGranter;
    }
}
