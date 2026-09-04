package org.hswebframework.web.oauth2.server.authentication;

import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Resolves one normalized OAuth2 client authentication request from an HTTP token request.
 *
 * <p>This is the injectable resolution facade. Applications may replace it completely, while the
 * default composite collects ordered {@link ReactiveOAuth2ClientAuthenticationRequestConverter}
 * contributors. Implementations must not mutate or retain the exchange or parameter map.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see CompositeReactiveOAuth2ClientAuthenticationRequestResolver
 */
public interface ReactiveOAuth2ClientAuthenticationRequestResolver {

    /**
     * Resolve authentication evidence for one token endpoint request.
     *
     * @param exchange current exchange; implementations must not retain it
     * @param parameters original query or form parameters; potentially contains credentials
     * @param grantType requested grant type
     * @return one normalized request, empty when this resolver does not recognize the evidence, or
     *         an error when recognized evidence is malformed; an error prevents resolver fallback
     * @implNote Resolution runs on subscription. Implementations may perform asynchronous work but
     * must not retain or mutate the exchange and parameter map; sensitive evidence belongs to the
     * returned request and must be erasable through
     * {@link OAuth2ClientAuthenticationRequest#eraseCredentials()}.
     */
    Mono<OAuth2ClientAuthenticationRequest> resolve(ServerWebExchange exchange,
                                                    MultiValueMap<String, String> parameters,
                                                    String grantType);
}
