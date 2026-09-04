package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Routes a client-credentials grant to the handler for the authenticated client type.
 *
 * <p>The handler registry is validated and frozen at construction time. Each request is dispatched
 * once by exact type; an unknown or missing type is rejected and never falls back to the default
 * handler.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ClientCredentialGrantHandler
 * @see DefaultClientCredentialGrantHandler
 */
public final class CompositeClientCredentialGranter implements ClientCredentialGranter {

    private final Map<String, ClientCredentialGrantHandler> handlers;

    /**
     * Create a router from the available typed handlers.
     *
     * @param handlers non-null handlers with stable, non-empty, unique client types
     * @throws NullPointerException if the collection or a handler is {@code null}
     * @throws IllegalArgumentException if a handler type is blank
     * @throws IllegalStateException if more than one handler declares the same type
     */
    public CompositeClientCredentialGranter(Collection<? extends ClientCredentialGrantHandler> handlers) {
        Objects.requireNonNull(handlers, "handlers must not be null");

        Map<String, ClientCredentialGrantHandler> mapping = new LinkedHashMap<>();
        for (ClientCredentialGrantHandler handler : handlers) {
            Objects.requireNonNull(handler, "handler must not be null");

            String clientType = handler.getClientType();
            if (clientType == null || clientType.isBlank()) {
                throw new IllegalArgumentException("client credential handler type must not be blank");
            }
            if (mapping.putIfAbsent(clientType, handler) != null) {
                throw new IllegalStateException("duplicate client credential handler type: " + clientType);
            }
        }
        this.handlers = Collections.unmodifiableMap(mapping);
    }

    @Override
    public Mono<AccessToken> requestToken(ClientCredentialRequest request) {
        return Mono.defer(() -> {
            OAuth2ClientAuthentication authentication = request.getClientAuthentication();
            ClientCredentialGrantHandler handler = authentication == null
                    ? null
                    : handlers.get(authentication.getClientType());
            if (handler == null) {
                return Mono.error(new OAuth2Exception(ErrorType.UNAUTHORIZED_CLIENT));
            }
            return handler.requestToken(request);
        });
    }
}
