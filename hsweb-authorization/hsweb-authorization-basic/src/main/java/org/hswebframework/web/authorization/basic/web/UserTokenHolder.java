package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.token.UserToken;

/**
 * TODO 完成注释
 *
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
