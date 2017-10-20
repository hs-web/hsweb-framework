package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.AuthenticationManager;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface ThirdPartAuthenticationManager extends AuthenticationManager {

    String getTokenType();
}
