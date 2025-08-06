package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ReactiveTokenAuthenticationSupplier implements ReactiveAuthenticationSupplier {

    private final TokenAuthenticationManager tokenManager;

    @Override
    public Mono<Authentication> get(String userId) {
        return Mono.empty();
    }

    @Override
    public Mono<Authentication> get() {
        return Mono
                .deferContextual(context -> context
                        .<ParsedToken>getOrEmpty(ParsedToken.class)
                        .map(t -> tokenManager.getByToken(t.getToken()))
                        .orElse(Mono.empty()));
    }
}
