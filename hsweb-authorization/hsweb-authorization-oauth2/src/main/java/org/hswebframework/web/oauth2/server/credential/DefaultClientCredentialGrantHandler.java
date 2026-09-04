package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Adapts the legacy user-backed {@link ClientCredentialGranter} as the default typed handler.
 *
 * <p>It declares {@link OAuth2Client#DEFAULT_CLIENT_TYPE} and delegates the selected
 * request unchanged. Handler selection and unknown-type rejection remain the responsibility of
 * {@link CompositeClientCredentialGranter}; this adapter performs no fallback.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ClientCredentialGrantHandler
 * @see CompositeClientCredentialGranter
 */
public final class DefaultClientCredentialGrantHandler implements ClientCredentialGrantHandler {

    private final ClientCredentialGranter delegate;

    /**
     * Create the default-type adapter.
     *
     * @param delegate non-null legacy granter invoked for selected default clients
     */
    public DefaultClientCredentialGrantHandler(ClientCredentialGranter delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public String getClientType() {
        return OAuth2Client.DEFAULT_CLIENT_TYPE;
    }

    @Override
    public Mono<AccessToken> requestToken(ClientCredentialRequest request) {
        return delegate.requestToken(request);
    }
}
