package org.hswebframework.web.oauth2.server;


import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenGranter;

public interface OAuth2GrantService {

    AuthorizationCodeGranter authorizationCode();

    ClientCredentialGranter clientCredential();

    RefreshTokenGranter refreshToken();
}
