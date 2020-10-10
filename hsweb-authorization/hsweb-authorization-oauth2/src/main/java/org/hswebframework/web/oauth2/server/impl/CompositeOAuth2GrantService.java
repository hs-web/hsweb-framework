package org.hswebframework.web.oauth2.server.impl;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.server.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.OAuth2GrantService;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;

@Getter
@Setter
public class CompositeOAuth2GrantService implements OAuth2GrantService {

    private AuthorizationCodeGranter authorizationCodeGranter;

    private ClientCredentialGranter clientCredentialGranter;

    @Override
    public AuthorizationCodeGranter authorizationCode() {
        return authorizationCodeGranter;
    }

    @Override
    public ClientCredentialGranter clientCredential() {
        return clientCredentialGranter;
    }
}
