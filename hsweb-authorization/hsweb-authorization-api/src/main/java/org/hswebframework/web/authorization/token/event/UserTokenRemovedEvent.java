package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.springframework.context.ApplicationEvent;

public class UserTokenRemovedEvent extends ApplicationEvent implements AuthorizationEvent {
    private UserToken detail;

    public UserTokenRemovedEvent(UserToken token) {
        super(token);
        this.detail = detail;
    }

    public UserToken getDetail() {
        return detail;
    }
}
