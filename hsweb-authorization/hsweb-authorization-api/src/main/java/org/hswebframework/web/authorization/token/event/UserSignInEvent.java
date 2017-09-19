package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;

/**
 * Created by zhouhao on 2017/7/7.
 */
public class UserSignInEvent implements AuthorizationEvent {
    private UserToken detail;


    public UserSignInEvent(UserToken detail) {
        this.detail = detail;
    }

    public UserToken getDetail() {
        return detail;
    }
}
