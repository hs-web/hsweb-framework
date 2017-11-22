package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.springframework.context.ApplicationEvent;

public class UserTokenChangedEvent extends ApplicationEvent implements AuthorizationEvent {
    private UserToken before, after;

    public UserTokenChangedEvent(UserToken before, UserToken after) {
        super(after);
        this.before = before;
        this.after = after;
    }

    public UserToken getBefore() {
        return before;
    }

    public UserToken getAfter() {
        return after;
    }
}
