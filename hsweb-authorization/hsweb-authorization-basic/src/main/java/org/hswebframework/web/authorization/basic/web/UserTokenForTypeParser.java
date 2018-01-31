package org.hswebframework.web.authorization.basic.web;

public interface UserTokenForTypeParser extends UserTokenParser {
    String getTokenType();
}
