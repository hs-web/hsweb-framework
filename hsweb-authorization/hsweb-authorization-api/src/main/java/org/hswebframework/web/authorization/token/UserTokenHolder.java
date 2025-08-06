package org.hswebframework.web.authorization.token;


import org.hswebframework.web.context.ContextHolder;
import reactor.util.context.Context;

import java.io.Closeable;

/**
 * @author zhouhao
 */
public final class UserTokenHolder {

    private UserTokenHolder() {
    }

    public static UserToken currentToken() {
        return ContextHolder
            .current()
            .getOrDefault(UserToken.class, null);
    }

    public static Closeable makeCurrent(UserToken token) {
      return ContextHolder.makeCurrent(Context.of(UserToken.class,token));
    }

}
