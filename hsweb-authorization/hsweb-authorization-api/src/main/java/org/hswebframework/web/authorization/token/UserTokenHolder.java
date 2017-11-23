package org.hswebframework.web.authorization.token;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.token.UserToken;

/**
 * @author zhouhao
 */
public final class UserTokenHolder {

    private UserTokenHolder() {
    }

    public static UserToken currentToken() {
        return ThreadLocalUtils.get(UserToken.class.getName());
    }

    public static UserToken setCurrent(UserToken token) {
        ThreadLocalUtils.put(UserToken.class.getName(), token);
        return token;
    }

}
