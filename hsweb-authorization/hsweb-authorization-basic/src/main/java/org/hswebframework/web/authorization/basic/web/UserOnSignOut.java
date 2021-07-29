package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.events.AuthorizationExitEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

/**
 * @author zhouhao
 */
public class UserOnSignOut {
    private final UserTokenManager userTokenManager;

    public UserOnSignOut(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    private String geToken() {
        UserToken token = UserTokenHolder.currentToken();
        return null != token ? token.getToken() : "";
    }

    @EventListener
    public void onApplicationEvent(AuthorizationExitEvent event) {
        event.async(userTokenManager.signOutByToken(geToken()));
    }
}
