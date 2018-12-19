package org.hswebframework.web.authorization.twofactor;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface TwoFactorTokenManager {
    TwoFactorToken getToken(String userId, String operation);
}
