package org.hswebframework.web.authorization.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.event.DefaultAsyncEvent;

@Getter
@Setter
@AllArgsConstructor
public class AuthorizationInitializeEvent extends DefaultAsyncEvent {

    private Authentication authentication;
}
