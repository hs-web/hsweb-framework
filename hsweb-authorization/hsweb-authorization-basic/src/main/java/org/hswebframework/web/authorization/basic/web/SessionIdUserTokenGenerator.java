package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhouhao
 */
public class SessionIdUserTokenGenerator implements UserTokenGenerator, Serializable {

    private static final long serialVersionUID = -9197243220777237431L;

    @Override
    public String getSupportTokenType() {
        return TOKEN_TYPE_SESSION_ID;
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        HttpServletRequest request = WebUtil.getHttpServletRequest();
        if (null == request) {
            throw new UnsupportedOperationException();
        }

        int timeout = request.getSession().getMaxInactiveInterval() * 1000;

        String sessionId = request.getSession().getId();

        return new GeneratedToken() {
            private static final long serialVersionUID = 3964183451883410929L;

            @Override
            public Map<String, Object> getResponse() {
                return new java.util.HashMap<>();
            }

            @Override
            public String getToken() {
                return sessionId;
            }

            @Override
            public String getType() {
                return TOKEN_TYPE_SESSION_ID;
            }

            @Override
            public int getTimeout() {
                return timeout;
            }
        };
    }
}
