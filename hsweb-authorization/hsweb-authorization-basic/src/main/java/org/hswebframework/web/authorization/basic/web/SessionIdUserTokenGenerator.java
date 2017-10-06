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
public class SessionIdUserTokenGenerator implements UserTokenGenerator ,Serializable {

    @Override
    public String getSupportTokenType() {
        return "sessionId";
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        HttpServletRequest request= WebUtil.getHttpServletRequest();
        if(null==request)throw new UnsupportedOperationException();


        int timeout =request.getSession().getMaxInactiveInterval()*1000;

        String sessionId = request.getSession().getId();

        return new GeneratedToken() {
            @Override
            public Map<String, Object> getResponse() {
                return Collections.emptyMap();
            }

            @Override
            public String getToken() {
                return sessionId;
            }

            @Override
            public int getTimeout() {
                return timeout;
            }
        };
    }
}
