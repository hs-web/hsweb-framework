package org.hswebframework.web.oauth2.server;


import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import reactor.core.publisher.Mono;

public interface OAuth2GrantService {

    AuthorizationCodeGranter code();

    ClientCredentialGranter clientCredential();

    Mono<Authentication> grant(String scope, Authentication authentication);
}
