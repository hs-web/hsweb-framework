package org.hswebframework.web.authorization.context;

import io.micrometer.context.ThreadLocalAccessor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.ReactiveAuthenticationHolder;

import javax.annotation.Nonnull;

public class AuthenticationThreadLocalAccessor
    implements ThreadLocalAccessor<Authentication> {

    static final Object KEY = Authentication.class;

    static {
        ReactiveAuthenticationHolder.addSupplier(
            new ThreadLocalReactiveAuthenticationSupplier()
        );
    }

    @Override
    @Nonnull
    public Object key() {
        return KEY;
    }

    @Override
    public Authentication getValue() {
        return AuthenticationHolder.get().orElse(null);
    }

    @Override
    public void setValue() {
        AuthenticationHolder.resetCurrent();
    }

    @Override
    public void setValue(@Nonnull Authentication value) {
        AuthenticationHolder.makeCurrent(value);
    }
}
