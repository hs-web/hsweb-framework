package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.hswebframework.web.authorization.token.UserToken;

import java.util.Optional;

/**
 * @author zhouhao
 */
public class UserTokenAuthenticationSupplier implements AuthenticationSupplier {

    private AuthenticationManager authenticationManager;

    public UserTokenAuthenticationSupplier(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication get(String userId) {
        if (userId == null) return null;
        return authenticationManager.getByUserId(userId);
    }

    protected UserToken getCurrentUserToken() {
        return UserTokenHolder.currentToken();
    }

    @Override
    public Authentication get() {
        return ThreadLocalUtils.get(Authentication.class.getName(), () ->
                get(Optional.ofNullable(getCurrentUserToken())
                        .filter(UserToken::validate) //验证token,如果不是正常状态,将会抛出异常
                        .map(UserToken::getUserId)
                        .orElse(null)));
    }
}
