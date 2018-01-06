package org.hswebframework.web.authorization.listener.event;

import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.HandleType;
import org.springframework.context.ApplicationEvent;

public class AuthorizingHandleBeforeEvent extends ApplicationEvent implements AuthorizationEvent {

    private static final long serialVersionUID = -1095765748533721998L;

    private boolean allow = false;

    private boolean execute = true;

    private String message;

    private HandleType handleType;

    public AuthorizingHandleBeforeEvent(AuthorizingContext context, HandleType handleType) {
        super(context);
        this.handleType = handleType;
    }

    public AuthorizingContext getContext() {
        return ((AuthorizingContext) getSource());
    }

    public boolean isExecute() {
        return execute;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean allow) {
        execute = false;
        this.allow = allow;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public HandleType getHandleType() {
        return handleType;
    }
}
