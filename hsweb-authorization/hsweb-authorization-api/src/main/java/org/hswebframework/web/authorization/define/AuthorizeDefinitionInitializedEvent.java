package org.hswebframework.web.authorization.define;

import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class AuthorizeDefinitionInitializedEvent extends ApplicationEvent implements AuthorizationEvent {
    private static final long serialVersionUID = -8185138454949381441L;

    public AuthorizeDefinitionInitializedEvent(List<AuthorizeDefinition> all) {
        super(all);
    }

    @SuppressWarnings("unchecked")
    public List<AuthorizeDefinition> getAllDefinition() {
        return ((List) getSource());
    }
}
