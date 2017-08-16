package org.hswebframework.web.authorization.basic.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.function.Predicate;

/**
 * @author zhouhao
 */
public class DefaultUserTokenParser implements UserTokenParser {
    @Override
    public String parseToken(HttpServletRequest request, Predicate<String> tokenValidate) {
        String token = request.getParameter("access_token");
        if (null != token) {
            if (tokenValidate.test(token))
                return token;
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            if (tokenValidate.test(session.getId()))
                return session.getId();
        }

        return null;
    }
}
