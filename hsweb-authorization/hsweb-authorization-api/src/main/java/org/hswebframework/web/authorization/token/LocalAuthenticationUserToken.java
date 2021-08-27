package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;


/**
 * 包含认证信息的用户令牌信息
 *
 * @author zhouhao
 * @since 4.0.12
 */
@AllArgsConstructor
public class LocalAuthenticationUserToken extends LocalUserToken implements AuthenticationUserToken {

    private final Authentication authentication;

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }
}
