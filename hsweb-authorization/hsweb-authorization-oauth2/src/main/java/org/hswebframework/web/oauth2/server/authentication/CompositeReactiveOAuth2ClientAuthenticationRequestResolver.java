package org.hswebframework.web.oauth2.server.authentication;

import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tries ordered client authentication request converters and uses the first recognized request.
 *
 * <p>Converters are invoked sequentially. Empty completion delegates to the next converter, while
 * malformed-input errors terminate resolution immediately. The built-in Secret converter is
 * appended as a final compatibility fallback.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 */
public class CompositeReactiveOAuth2ClientAuthenticationRequestResolver
    implements ReactiveOAuth2ClientAuthenticationRequestResolver {

    private final List<ReactiveOAuth2ClientAuthenticationRequestConverter> converters;

    public CompositeReactiveOAuth2ClientAuthenticationRequestResolver(
        Collection<? extends ReactiveOAuth2ClientAuthenticationRequestConverter> customConverters,
        ReactiveOAuth2ClientAuthenticationRequestConverter fallbackConverter) {
        List<ReactiveOAuth2ClientAuthenticationRequestConverter> all = new ArrayList<>();
        if (customConverters != null) {
            all.addAll(customConverters);
        }
        all.add(Objects.requireNonNull(fallbackConverter, "fallbackConverter must not be null"));
        this.converters = Collections.unmodifiableList(all);
    }

    @Override
    public Mono<OAuth2ClientAuthenticationRequest> resolve(ServerWebExchange exchange,
                                                           MultiValueMap<String, String> parameters,
                                                           String grantType) {
        return Flux
            .fromIterable(converters)
            .concatMap(converter -> Mono.defer(() -> converter.convert(exchange, parameters, grantType)), 1)
            .next();
    }
}
