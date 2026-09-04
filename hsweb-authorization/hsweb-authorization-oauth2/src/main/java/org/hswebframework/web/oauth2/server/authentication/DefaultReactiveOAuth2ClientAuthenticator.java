package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import reactor.core.publisher.Mono;

/**
 * Compatibility facade for the default shared-secret client authentication provider.
 *
 * <p>Applications that need multiple authentication methods should use
 * {@link CompositeReactiveOAuth2ClientAuthenticator}; this class preserves direct construction
 * with {@link OAuth2ClientManager} for legacy controller and test usage.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ReactiveOAuth2ClientAuthenticator
 * @see OAuth2ClientAuthentication
 */
public class DefaultReactiveOAuth2ClientAuthenticator implements ReactiveOAuth2ClientAuthenticator {

    private final DefaultOAuth2ClientSecretAuthenticationProvider provider;

    public DefaultReactiveOAuth2ClientAuthenticator(OAuth2ClientManager clientManager) {
        this.provider = new DefaultOAuth2ClientSecretAuthenticationProvider(clientManager);
    }

    @Override
    public Mono<OAuth2ClientAuthentication> authenticate(OAuth2ClientAuthenticationRequest request) {
        if (request == null || !provider.getAuthenticationMethods().contains(request.getAuthenticationMethod())) {
            return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_AUTHORIZATION));
        }
        return provider.authenticate(request);
    }
}
