package org.hswebframework.web.authorization.basic.embed;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * @author zhouhao
 * @since 4.0.0
 */
@Order(10)
@AllArgsConstructor
public class EmbedReactiveAuthenticationManager implements ReactiveAuthenticationManagerProvider {

    private final EmbedAuthenticationProperties properties;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        if (MapUtils.isEmpty(properties.getUsers())) {
            return Mono.empty();
        }
        return request.
                handle((req, sink) -> {
                    Authentication auth = properties.authenticate(req);
                    if (auth != null) {
                        sink.next(auth);
                    }
                });

    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        return Mono.justOrEmpty(properties.getAuthentication(userId));
    }


}
