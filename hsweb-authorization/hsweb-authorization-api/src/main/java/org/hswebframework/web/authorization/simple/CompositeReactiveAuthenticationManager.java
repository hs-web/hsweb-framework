package org.hswebframework.web.authorization.simple;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CompositeReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private List<ReactiveAuthenticationManagerProvider> providers;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return Flux.concat(providers.stream()
                .map(manager -> manager
                        .authenticate(request)
                        .onErrorResume((err) -> {
                            return Mono.empty();
                        })).collect(Collectors.toList()))
                .take(1)
                .next();
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
