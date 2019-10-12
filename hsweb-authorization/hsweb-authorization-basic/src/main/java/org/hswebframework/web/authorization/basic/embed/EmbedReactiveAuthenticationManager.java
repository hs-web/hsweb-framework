package org.hswebframework.web.authorization.basic.embed;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
public class EmbedReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private EmbedAuthenticationProperties properties;

    @Override
    public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
        return request.map(properties::authenticate);

    }

    @Override
    public Mono<Authentication> getByUserId(String userId) {
        return Mono.justOrEmpty(properties.getAuthentication(userId));
    }


}
