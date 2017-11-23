package org.hswebframework.web.authorization.basic.web.session;

import org.hswebframework.web.authorization.token.UserTokenManager;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UserTokenAutoExpiredListener implements HttpSessionListener {

    private UserTokenManager userTokenManager;

    public UserTokenAutoExpiredListener(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        userTokenManager.signOutByToken(sessionId);
    }
}
