package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.event.AuthorizationExitEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class UserOnSignOut implements AuthorizationListener<AuthorizationExitEvent> {
    private UserTokenManager userTokenManager;

    public UserOnSignOut(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Override
    public void on(AuthorizationExitEvent event) {
        userTokenManager.signOutByToken(geToken());
    }

    protected String geToken() {
        UserToken token = UserTokenHolder.currentToken();
        return null != token ? token.getToken() : null;
    }
}
