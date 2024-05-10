package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.events.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.event.DefaultAsyncEvent;

public class UserTokenRemovedEvent extends DefaultAsyncEvent implements AuthorizationEvent {

    private static final long serialVersionUID = -6662943150068863177L;

   private final UserToken token;

    public UserTokenRemovedEvent(UserToken token) {
        this.token=token;
    }

    public UserToken getDetail() {
        return token;
    }
}
