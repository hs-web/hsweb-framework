package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Routes normalized client authentication requests to exactly one provider.
 *
 * <p>Custom providers are registered first and may replace an internal fallback method. Duplicate
 * methods between custom providers fail construction. Once selected, provider failure or empty
 * completion terminates the request and never triggers another authentication strategy.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ReactiveOAuth2ClientAuthenticationProvider
 */
public class CompositeReactiveOAuth2ClientAuthenticator implements ReactiveOAuth2ClientAuthenticator {

    private final Map<String, ReactiveOAuth2ClientAuthenticationProvider> providers;

    public CompositeReactiveOAuth2ClientAuthenticator(
        Collection<? extends ReactiveOAuth2ClientAuthenticationProvider> customProviders,
        ReactiveOAuth2ClientAuthenticationProvider fallbackProvider) {
        Map<String, ReactiveOAuth2ClientAuthenticationProvider> mappings = new LinkedHashMap<>();
        if (customProviders != null) {
            customProviders.forEach(provider -> register(mappings, provider, false));
        }
        register(mappings, Objects.requireNonNull(fallbackProvider, "fallbackProvider must not be null"), true);
        this.providers = Collections.unmodifiableMap(mappings);
    }

    @Override
    public Mono<OAuth2ClientAuthentication> authenticate(OAuth2ClientAuthenticationRequest request) {
        if (request == null || !StringUtils.hasText(request.getAuthenticationMethod())) {
            return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_AUTHORIZATION));
        }
        ReactiveOAuth2ClientAuthenticationProvider provider =
            providers.get(request.getAuthenticationMethod());
        if (provider == null) {
            return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_AUTHORIZATION));
        }
        return Mono
            .defer(() -> provider.authenticate(request))
            .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.UNAUTHORIZED_CLIENT)));
    }

    private static void register(
        Map<String, ReactiveOAuth2ClientAuthenticationProvider> mappings,
        ReactiveOAuth2ClientAuthenticationProvider provider,
        boolean fallback) {
        Objects.requireNonNull(provider, "provider must not be null");
        Collection<String> methods = provider.getAuthenticationMethods();
        if (methods == null || methods.isEmpty()) {
            throw new IllegalStateException("oauth2 client authentication provider methods must not be empty");
        }
        for (String method : methods) {
            if (!StringUtils.hasText(method)) {
                throw new IllegalStateException("oauth2 client authentication method must not be empty");
            }
            if (fallback) {
                mappings.putIfAbsent(method, provider);
            } else if (mappings.putIfAbsent(method, provider) != null) {
                throw new IllegalStateException(
                    "duplicate oauth2 client authentication provider method: " + method);
            }
        }
    }
}
