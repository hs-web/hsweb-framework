package org.hswebframework.web.authorization.basic.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.function.Predicate;

/**
 * @author zhouhao
 */
public class SessionIdUserTokenParser implements UserTokenParser {
    @Override
    public ParsedToken parseToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            return session::getId;
        }

        return null;
    }
}
