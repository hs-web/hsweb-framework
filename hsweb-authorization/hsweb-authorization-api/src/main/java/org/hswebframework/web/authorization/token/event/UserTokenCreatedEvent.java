package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.events.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.event.DefaultAsyncEvent;

public class UserTokenCreatedEvent extends DefaultAsyncEvent implements AuthorizationEvent {
    private final UserToken detail;

    public UserTokenCreatedEvent(UserToken detail) {
        this.detail = detail;
    }

    public UserToken getDetail() {
        return detail;
    }
}
