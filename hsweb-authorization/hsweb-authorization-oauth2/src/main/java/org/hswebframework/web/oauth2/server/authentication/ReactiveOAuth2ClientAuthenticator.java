package org.hswebframework.web.oauth2.server.authentication;

import reactor.core.publisher.Mono;

/**
 * Authenticates an OAuth2 client during token endpoint processing.
 *
 * <p>The token endpoint invokes this strategy after HTTP authentication evidence has been
 * normalized. Each authentication method owns its required identifier and credential validation;
 * the controller does not assume a shared secret. Request parameters never contain raw
 * credentials.</p>
 *
 * <p>An implementation must emit exactly one non-null {@link OAuth2ClientAuthentication} or fail
 * with an authentication error. It must not use {@link Mono#empty()} to request another strategy:
 * empty completion is an authentication failure and never enables fallback. Errors propagate to
 * the token endpoint unchanged. Implementations must not retain, log, or otherwise expose the
 * request credentials.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see DefaultReactiveOAuth2ClientAuthenticator
 * @see OAuth2ClientAuthentication
 */
public interface ReactiveOAuth2ClientAuthenticator {

    /**
     * Authenticate one token endpoint request.
     *
     * @param request non-null normalized request with sanitized parameters; implementations must
     *                not retain or mutate its security-sensitive state
     * @return a publisher that emits exactly one authenticated client context; empty completion is
     *         not a fallback signal and must be treated as authentication failure, while errors are
     *         propagated to the token endpoint
     */
    Mono<OAuth2ClientAuthentication> authenticate(OAuth2ClientAuthenticationRequest request);

}
