package org.hswebframework.web.authorization.context;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import reactor.core.publisher.Mono;

class ThreadLocalReactiveAuthenticationSupplier implements ReactiveAuthenticationSupplier {
    @Override
    public Mono<Authentication> get(String userId) {
        return Mono.empty();
    }

    @Override
    public Mono<Authentication> get() {
        return Mono.justOrEmpty(AuthenticationHolder.get());
    }
}
