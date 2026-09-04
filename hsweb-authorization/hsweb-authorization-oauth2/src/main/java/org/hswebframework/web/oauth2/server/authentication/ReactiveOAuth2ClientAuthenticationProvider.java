package org.hswebframework.web.oauth2.server.authentication;

import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Verifies normalized OAuth2 client authentication evidence for one or more methods.
 *
 * <p>Providers run after HTTP evidence extraction and do not access the web exchange. The
 * composite authenticator selects exactly one provider by authentication method; an error or empty
 * result terminates authentication and never falls back to another provider.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see CompositeReactiveOAuth2ClientAuthenticator
 * @see DefaultOAuth2ClientSecretAuthenticationProvider
 */
public interface ReactiveOAuth2ClientAuthenticationProvider {

    /**
     * Return the open method identifiers handled by this provider during authenticator assembly.
     *
     * @return deterministic, non-empty method identifiers; duplicates between custom providers are
     *         invalid
     */
    Collection<String> getAuthenticationMethods();

    /**
     * Authenticate a request already selected by one of {@link #getAuthenticationMethods()}.
     *
     * @param request normalized request; implementations must not retain it or expose credentials
     * @return exactly one authenticated client context; empty completion is an authentication
     *         failure and errors propagate without fallback
     * @implNote Providers may perform asynchronous verification. They must validate the concrete
     * request type they accept, erase temporary credential copies on every terminal signal, and
     * leave cleanup of the request-owned evidence to the token endpoint.
     */
    Mono<OAuth2ClientAuthentication> authenticate(OAuth2ClientAuthenticationRequest request);
}
