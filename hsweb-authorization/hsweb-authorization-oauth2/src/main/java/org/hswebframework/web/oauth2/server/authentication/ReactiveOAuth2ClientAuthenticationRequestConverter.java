package org.hswebframework.web.oauth2.server.authentication;

import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Converts one HTTP credential representation into a normalized client authentication request.
 *
 * <p>Converters are ordered contributors used by the default request resolver. They own
 * transport-specific extraction only; client verification belongs to a
 * {@link ReactiveOAuth2ClientAuthenticationProvider}.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see CompositeReactiveOAuth2ClientAuthenticationRequestResolver
 * @see ClientSecretAuthenticationRequestConverter
 */
public interface ReactiveOAuth2ClientAuthenticationRequestConverter {

    /**
     * Convert authentication evidence for one token endpoint request.
     *
     * @param exchange current exchange; implementations must not retain it
     * @param parameters original query or form parameters; potentially contains credentials
     * @param grantType requested grant type
     * @return one normalized request, empty when this converter does not recognize the evidence,
     *         or an error when recognized evidence is malformed; errors prevent fallback
     * @implNote The default resolver invokes converters sequentially after subscription. A
     * recognized converter owns validation of its transport format and must place every sensitive
     * copy in a request whose {@link OAuth2ClientAuthenticationRequest#eraseCredentials()} clears it.
     */
    Mono<OAuth2ClientAuthenticationRequest> convert(ServerWebExchange exchange,
                                                    MultiValueMap<String, String> parameters,
                                                    String grantType);
}
