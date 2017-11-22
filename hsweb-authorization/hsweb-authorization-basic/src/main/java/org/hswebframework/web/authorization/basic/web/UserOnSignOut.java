package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.event.AuthorizationExitEvent;
import org.hswebframework.web.authorization.listener.event.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.context.ApplicationListener;

/**
 * @author zhouhao
 */
public class UserOnSignOut implements AuthorizationListener<AuthorizationExitEvent>,ApplicationListener<AuthorizationExitEvent> {
    private UserTokenManager userTokenManager;

    public UserOnSignOut(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Override
    public void on(AuthorizationExitEvent event) {
       onApplicationEvent(event);
    }

    private String geToken() {
        UserToken token = UserTokenHolder.currentToken();
        return null != token ? token.getToken() : "";
    }

    @Override
    public void onApplicationEvent(AuthorizationExitEvent event) {
        userTokenManager.signOutByToken(geToken());
    }
}
