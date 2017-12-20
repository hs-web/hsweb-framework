package org.hswebframework.web.authorization.jwt;

import org.hswebframework.web.authorization.basic.web.AuthorizedToken;

/**
 *
 * @author zhouhao
 */
public class JwtAuthorizedToken implements AuthorizedToken {

    public static final String TOKEN_TYPE = "jwt";

    private String token;

    private String userId;

    public JwtAuthorizedToken() {
    }

    public JwtAuthorizedToken(String token, String userId) {
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

    @Override
    public String getType() {
        return TOKEN_TYPE;
    }
}
