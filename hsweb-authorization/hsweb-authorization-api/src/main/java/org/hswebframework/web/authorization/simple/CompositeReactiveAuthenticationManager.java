package org.hswebframework.web.authorization.simple;

import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CompositeReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final List<ReactiveAuthenticationManagerProvider> providers;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return Flux.concat(providers
                                   .stream()
                                   .map(manager -> manager
                                           .authenticate(request)
                                           .onErrorResume((err) -> Mono.empty()))
                                   .collect(Collectors.toList()))
                   .take(1)
                   .next();
    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        return Flux
                .fromStream(providers
                                    .stream()
                                    .map(manager -> manager
                                            .getByUserId(userId)
                                            .onErrorResume((err) -> Mono.empty())
                                    ))
                .flatMap(Function.identity())
                .collectList()
                .filter(CollectionUtils::isNotEmpty)
                .map(all -> {
                    if (all.size() == 1) {
                        return all.get(0);
                    }
                    SimpleAuthentication authentication = new SimpleAuthentication();
                    for (Authentication auth : all) {
                        authentication.merge(auth);
                    }
                    return authentication;
                });
    }
}
