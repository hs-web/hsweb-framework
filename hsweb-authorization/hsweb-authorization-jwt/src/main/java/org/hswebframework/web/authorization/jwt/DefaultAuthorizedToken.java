package org.hswebframework.web.authorization.jwt;

import org.hswebframework.web.authorization.basic.web.AuthorizedToken;

/**
 *
 * @author zhouhao
 */
public class DefaultAuthorizedToken implements AuthorizedToken {
    private String token;

    private String userId;

    public DefaultAuthorizedToken() {
    }

    public DefaultAuthorizedToken(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
