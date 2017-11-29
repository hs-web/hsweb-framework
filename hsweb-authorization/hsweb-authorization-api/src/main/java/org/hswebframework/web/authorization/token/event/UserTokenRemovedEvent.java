package org.hswebframework.web.authorization.token.event;

import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.springframework.context.ApplicationEvent;

public class UserTokenRemovedEvent extends ApplicationEvent implements AuthorizationEvent {

    private static final long serialVersionUID = -6662943150068863177L;

    public UserTokenRemovedEvent(UserToken token) {
        super(token);
    }

    public UserToken getDetail() {
        return ((UserToken) getSource());
    }
}
