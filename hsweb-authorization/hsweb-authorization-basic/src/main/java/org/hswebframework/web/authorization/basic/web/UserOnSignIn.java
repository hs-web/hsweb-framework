package org.hswebframework.web.authorization.basic.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.event.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;

import java.util.Optional;
import java.util.UUID;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class UserOnSignIn implements AuthorizationListener<AuthorizationSuccessEvent> {
    private UserTokenManager userTokenManager;

    public UserOnSignIn(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }
    @Override
    public void on(AuthorizationSuccessEvent event) {
        UserToken token = UserTokenHolder.currentToken();
        String tokenType = (String) event.getParameter("token_type").orElse("sessionId");

        if (token != null) {
            userTokenManager.signOutByToken(token.getToken());
        }
        token = userTokenManager.signIn(createToken(tokenType), event.getAuthentication().getUser().getId());
        event.getResult().put("token", token.getToken());

    }

    protected String createToken(String type) {
        switch (type) {
            case "simple":
                return DigestUtils.md5Hex(UUID.randomUUID().toString().concat(String.valueOf(Math.random())));
            default:
                return Optional.ofNullable(WebUtil.getHttpServletRequest())
                        .orElseThrow(UnsupportedOperationException::new)
                        .getSession()
                        .getId();
        }

    }
}
