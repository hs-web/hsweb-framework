package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.server.AccessToken;
import reactor.core.publisher.Mono;

/**
 * Issues client-credentials tokens for one authenticated client type.
 *
 * <p>After client authentication succeeds, {@link CompositeClientCredentialGranter} selects one
 * handler by exact {@link #getClientType()} match and invokes it once. Types must be stable,
 * non-empty, and unique within the application. There is no fallback to another type when a
 * handler is absent, completes empty, or fails.</p>
 *
 * <p>The token endpoint supplies a sanitized authentication context. Implementations must treat it
 * as read-only, must not retain security-sensitive request state, and must never reconstruct,
 * persist, or expose client credentials.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see CompositeClientCredentialGranter
 * @see DefaultClientCredentialGrantHandler
 */
public interface ClientCredentialGrantHandler {

    /**
     * Return the exact authenticated client type supported by this handler.
     *
     * @return stable, non-empty type that is unique within the application context
     */
    String getClientType();

    /**
     * Issue an access token after the client has been authenticated for this handler's type.
     *
     * @param request non-null, sanitized client-credentials context; implementations must not
     *                mutate or retain its security-sensitive state
     * @return publisher for the issued token; empty completion means that no token is issued and
     *         errors propagate to the token endpoint; neither outcome triggers fallback
     */
    Mono<AccessToken> requestToken(ClientCredentialRequest request);
}
