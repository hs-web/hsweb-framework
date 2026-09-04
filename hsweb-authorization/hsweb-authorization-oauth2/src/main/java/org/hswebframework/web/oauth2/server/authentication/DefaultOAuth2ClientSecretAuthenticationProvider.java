package org.hswebframework.web.oauth2.server.authentication;

import lombok.AllArgsConstructor;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Default provider for {@code client_secret_basic} and {@code client_secret_post}.
 *
 * <p>It preserves the legacy {@link OAuth2ClientManager} lookup and secret validation behavior.
 * Request extraction and grant processing are outside this provider.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 */
@AllArgsConstructor
public class DefaultOAuth2ClientSecretAuthenticationProvider
    implements ReactiveOAuth2ClientAuthenticationProvider {

    private static final Collection<String> METHODS = Arrays.asList(
        OAuth2ClientAuthenticationRequest.CLIENT_SECRET_BASIC,
        OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST);

    private final OAuth2ClientManager clientManager;

    @Override
    public Collection<String> getAuthenticationMethods() {
        return METHODS;
    }

    @Override
    public Mono<OAuth2ClientAuthentication> authenticate(OAuth2ClientAuthenticationRequest request) {
        return Mono.defer(() -> {
            if (!(request instanceof OAuth2ClientSecretAuthenticationRequest)) {
                return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_AUTHORIZATION));
            }
            if (!StringUtils.hasText(request.getClientId())) {
                return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID));
            }
            OAuth2ClientSecretAuthenticationRequest secretRequest =
                (OAuth2ClientSecretAuthenticationRequest) request;
            char[] clientSecret = secretRequest.getClientSecret();
            if (clientSecret == null || clientSecret.length == 0) {
                return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_SECRET));
            }
            // using guarantees erasure if lookup completes, fails, is cancelled, or throws.
            return Mono.using(
                () -> clientSecret,
                secret -> clientManager
                    .getClient(request.getClientId())
                    .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID)))
                    .map(client -> {
                        client.validateSecret(new String(secret));
                        return new OAuth2ClientAuthentication(
                            client,
                            client.getClientType(),
                            request.getAuthenticationMethod(),
                            Collections.emptyMap());
                    }),
                secret -> Arrays.fill(secret, '\0'));
        });
    }
}
