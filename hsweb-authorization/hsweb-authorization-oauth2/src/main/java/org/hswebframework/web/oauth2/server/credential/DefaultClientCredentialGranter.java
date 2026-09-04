package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationHolder;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.oauth2.GrantType;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.event.OAuth2GrantedEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

public class DefaultClientCredentialGranter implements ClientCredentialGranter {

    private final AccessTokenManager accessTokenManager;

    private final ApplicationEventPublisher eventPublisher;

    private final ReactiveAuthenticationManager authenticationManager;

    public DefaultClientCredentialGranter(AccessTokenManager accessTokenManager,
                                          ApplicationEventPublisher eventPublisher) {
        this.accessTokenManager = accessTokenManager;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = null;
    }

    /**
     * Create the legacy granter with an explicit authentication manager.
     *
     * <p>This constructor is retained for source and binary compatibility. It continues to resolve
     * user authorization through the supplied {@link ReactiveAuthenticationManager#getByUserId(String)}.
     * New code and auto-configuration should use
     * {@link #DefaultClientCredentialGranter(AccessTokenManager, ApplicationEventPublisher)}, which
     * resolves authorization through {@link ReactiveAuthenticationHolder}.</p>
     *
     * @param authenticationManager authentication manager used to resolve the client's user
     * @param accessTokenManager access-token manager
     * @param eventPublisher OAuth2 grant event publisher
     * @deprecated use {@link #DefaultClientCredentialGranter(AccessTokenManager, ApplicationEventPublisher)}
     */
    @Deprecated
    public DefaultClientCredentialGranter(ReactiveAuthenticationManager authenticationManager,
                                          AccessTokenManager accessTokenManager,
                                          ApplicationEventPublisher eventPublisher) {
        this.accessTokenManager = accessTokenManager;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<AccessToken> requestToken(ClientCredentialRequest request) {

        OAuth2Client client = request.getClient();

        Mono<Authentication> authReq =
            (authenticationManager == null
                ? ReactiveAuthenticationHolder
                  .get(client.getUserId())
                : authenticationManager.getByUserId(client.getUserId()));

        return authReq
            .flatMap(auth -> accessTokenManager
                .createAccessToken(client.getClientId(), auth, true)
                .flatMap(token ->
                             new OAuth2GrantedEvent(client,
                                                    token,
                                                    auth,
                                                    "*",
                                                    GrantType.client_credentials,
                                                    request.getParameters())
                                 .publish(eventPublisher)
                                 .onErrorResume(err -> accessTokenManager
                                     .removeToken(client.getClientId(), token.getAccessToken())
                                     .then(Mono.error(err)))
                                 .thenReturn(token))
            );
    }
}
