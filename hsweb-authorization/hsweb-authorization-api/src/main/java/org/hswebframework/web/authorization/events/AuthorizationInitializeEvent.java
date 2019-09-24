package org.hswebframework.web.authorization.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.Authentication;

@Getter
@AllArgsConstructor
public class AuthorizationInitializeEvent {

    private Authentication authentication;
}
