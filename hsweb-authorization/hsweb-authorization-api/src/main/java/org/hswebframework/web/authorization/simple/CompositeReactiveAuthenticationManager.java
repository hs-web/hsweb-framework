package org.hswebframework.web.authorization.simple;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
public class CompositeReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private List<ReactiveAuthenticationManagerProvider> providers;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return Flux
                .fromStream(providers.stream()
                        .map(manager -> manager
                                .authenticate(request)
                                .onErrorResume((err) -> {
                                    return Mono.empty();
                                })
                        ))
                .flatMap(Function.identity())
                .reduceWith(SimpleAuthentication::of, Authentication::merge)
                .filter(a -> a.getUser() != null);
    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        return Flux
                .fromStream(providers.stream()
                        .map(manager -> manager
                                .getByUserId(userId)
                                .onErrorResume((err) -> {
                                    return Mono.empty();
                                })
                        ))
                .flatMap(Function.identity())
                .reduceWith(SimpleAuthentication::of, Authentication::merge)
                .filter(a -> a.getUser() != null);
    }
}
