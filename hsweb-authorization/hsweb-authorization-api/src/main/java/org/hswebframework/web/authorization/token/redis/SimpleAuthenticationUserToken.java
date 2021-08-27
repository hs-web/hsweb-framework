package org.hswebframework.web.authorization.token.redis;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.AuthenticationUserToken;

@AllArgsConstructor
public class SimpleAuthenticationUserToken  extends SimpleUserToken implements AuthenticationUserToken {
    private final Authentication authentication;

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }
}
