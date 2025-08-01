package org.hswebframework.web.authorization.token;


import org.hswebframework.web.context.ContextHolder;
import org.hswebframework.web.context.ContextUtils;

/**
 * @author zhouhao
 */
public final class UserTokenHolder {

    private UserTokenHolder() {
    }

    public static UserToken currentToken() {
        return ContextHolder.current().getOrDefault(UserToken.class,null);
    }

    public static UserToken setCurrent(UserToken token) {
        ContextUtils.currentContext().put(UserToken.class, token);
        return token;
    }

}
