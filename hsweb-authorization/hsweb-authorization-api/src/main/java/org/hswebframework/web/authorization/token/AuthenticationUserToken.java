package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;

/**
 * 包含认证信息的token
 *
 * @author zhouhao
 * @since 4.0.12
 */
public interface AuthenticationUserToken extends UserToken {

    /**
     * 获取认证信息
     *
     * @return auth
     * @see Authentication
     */
    Authentication getAuthentication();

}
