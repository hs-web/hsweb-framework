package org.hswebframework.web.oauth2.server;


import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;

public interface OAuth2GrantService {

    AuthorizationCodeGranter authorizationCode();

    ClientCredentialGranter clientCredential();

}
