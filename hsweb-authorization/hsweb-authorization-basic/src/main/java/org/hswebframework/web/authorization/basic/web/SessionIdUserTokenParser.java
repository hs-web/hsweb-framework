package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.function.Predicate;

import static org.hswebframework.web.authorization.basic.web.UserTokenGenerator.TOKEN_TYPE_SESSION_ID;

/**
 * @author zhouhao
 */
public class SessionIdUserTokenParser implements UserTokenParser {


    protected UserTokenManager userTokenManager;

    @Autowired
    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String sessionId = session.getId();
            UserToken token = userTokenManager.getByToken(sessionId);
            long interval = session.getMaxInactiveInterval();
            //当前已登录token已失效但是session未失效
            if (token != null && token.isExpired()) {
                String userId = token.getUserId();
                return new AuthorizedToken() {
                    @Override
                    public String getUserId() {
                        return userId;
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
                    public long getMaxInactiveInterval() {
                        return interval;
                    }
                };
            }
            return new ParsedToken() {
                @Override
                public String getToken() {
                    return session.getId();
                }

                @Override
                public String getType() {
                    return TOKEN_TYPE_SESSION_ID;
                }
            };
        }
        return null;
    }
}
