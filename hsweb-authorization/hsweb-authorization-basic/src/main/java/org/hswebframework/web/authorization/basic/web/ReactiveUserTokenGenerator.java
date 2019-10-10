package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.Authentication;

public interface ReactiveUserTokenGenerator {

    String getTokenType();

    GeneratedToken generate(Authentication authentication);
}
