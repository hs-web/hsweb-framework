package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.events.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.event.DefaultAsyncEvent;

public class UserTokenChangedEvent extends DefaultAsyncEvent implements AuthorizationEvent {
    private final UserToken before, after;

    public UserTokenChangedEvent(UserToken before, UserToken after) {
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
