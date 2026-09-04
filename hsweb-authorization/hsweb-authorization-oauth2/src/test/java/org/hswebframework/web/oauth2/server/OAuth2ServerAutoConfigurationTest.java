package org.hswebframework.web.oauth2.server;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.oauth2.server.authentication.CompositeReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeRequest;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeResponse;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeTokenRequest;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGrantHandler;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialRequest;
import org.hswebframework.web.oauth2.server.credential.CompositeClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.DefaultClientCredentialGranter;
import org.hswebframework.web.oauth2.server.web.OAuth2AuthorizeController;
import org.junit.Test;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OAuth2ServerAutoConfigurationTest {

    @Test
    public void shouldKeepCustomLegacyFacadeAsTheOnlyGranter() {
        AccessToken expected = new AccessToken("legacy", null, 60);
        ClientCredentialGranter legacy = request -> Mono.just(expected);

        try (AnnotationConfigReactiveWebApplicationContext context = contextWithInfrastructure()) {
            context.getBeanFactory().registerSingleton("legacyClientCredentialGranter", legacy);
            context.getBeanFactory().registerSingleton("apiClientCredentialGrantHandler",
                                                       handler("api", new AccessToken("api", null, 60)));
            refresh(context);

            Map<String, ClientCredentialGranter> granters = context.getBeansOfType(ClientCredentialGranter.class);
            assertEquals(1, granters.size());
            assertSame(legacy, granters.values().iterator().next());
            assertFalse(context.containsBean("clientCredentialGranter"));

            StepVerifier
                    .create(context.getBean(OAuth2GrantService.class)
                                   .clientCredential()
                                   .requestToken(request("api")))
                    .expectNext(expected)
                    .verifyComplete();
        }
    }

    @Test
    public void shouldCombineBuiltInDefaultAndExternalHandlersBehindOneFacade() {
        AccessToken expected = new AccessToken("api", null, 60);

        try (AnnotationConfigReactiveWebApplicationContext context = contextWithInfrastructure()) {
            context.getBeanFactory().registerSingleton("apiClientCredentialGrantHandler", handler("api", expected));
            refresh(context);

            Map<String, ClientCredentialGranter> granters = context.getBeansOfType(ClientCredentialGranter.class);
            assertEquals(1, granters.size());
            assertTrue(granters.values().iterator().next() instanceof CompositeClientCredentialGranter);
            assertSame(granters.values().iterator().next(), context.getBean("clientCredentialGranter"));

            StepVerifier
                    .create(granters.values().iterator().next().requestToken(request("api")))
                    .expectNext(expected)
                    .verifyComplete();
        }
    }

    @Test
    public void shouldFailStartupWhenExternalHandlerUsesDefaultType() {
        AnnotationConfigReactiveWebApplicationContext context = contextWithInfrastructure();
        context.getBeanFactory().registerSingleton(
                "conflictingDefaultHandler",
                handler(OAuth2Client.DEFAULT_CLIENT_TYPE, new AccessToken()));

        try {
            refresh(context);
            fail("expected duplicate default client type to fail startup");
        } catch (RuntimeException error) {
            assertTrue(hasMessage(error, "duplicate client credential handler type: default"));
        } finally {
            context.close();
        }
    }

    @Test
    public void shouldProvideDefaultAuthenticatorAndInjectItIntoController() {
        try (AnnotationConfigReactiveWebApplicationContext context = contextWithInfrastructure()) {
            context.getBeanFactory().registerSingleton(
                    "oAuth2ClientManager",
                    (OAuth2ClientManager) clientId -> Mono.empty());
            refresh(context);

            ReactiveOAuth2ClientAuthenticator authenticator =
                    context.getBean(ReactiveOAuth2ClientAuthenticator.class);
            assertTrue(authenticator instanceof CompositeReactiveOAuth2ClientAuthenticator);
            assertSame(context.getBean(OAuth2AuthorizeController.class),
                       context.getBean("oAuth2AuthorizeController"));
        }
    }

    @Test
    public void shouldUseManagerForLegacyThreeArgumentGranterConstructor() {
        Authentication expectedAuthentication = SimpleAuthentication.of();
        AccessToken expectedToken = new AccessToken("legacy-token", null, 60);
        AtomicInteger managerCalls = new AtomicInteger();
        AtomicReference<String> requestedUserId = new AtomicReference<>();
        ReactiveAuthenticationManager authenticationManager = new ReactiveAuthenticationManager() {
            @Override
            public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
                return Mono.error(new AssertionError("authenticate must not be used"));
            }

            @Override
            public Mono<Authentication> getByUserId(String userId) {
                managerCalls.incrementAndGet();
                requestedUserId.set(userId);
                return Mono.just(expectedAuthentication);
            }
        };
        AtomicInteger createCalls = new AtomicInteger();
        AtomicReference<String> requestedClientId = new AtomicReference<>();
        AtomicReference<Authentication> tokenAuthentication = new AtomicReference<>();
        AtomicReference<Boolean> singleton = new AtomicReference<>();
        AccessTokenManager accessTokenManager = new AccessTokenManager() {
            @Override
            public Mono<Authentication> getAuthenticationByToken(String accessToken) {
                return Mono.empty();
            }

            @Override
            public Mono<AccessToken> createAccessToken(String clientId,
                                                       Authentication authentication,
                                                       boolean singletonToken) {
                createCalls.incrementAndGet();
                requestedClientId.set(clientId);
                tokenAuthentication.set(authentication);
                singleton.set(singletonToken);
                return Mono.just(expectedToken);
            }

            @Override
            public Mono<AccessToken> refreshAccessToken(String clientId, String refreshToken) {
                return Mono.empty();
            }

            @Override
            public Mono<Void> removeToken(String clientId, String token) {
                return Mono.empty();
            }

            @Override
            public Mono<Void> cancelGrant(String clientId, String userId) {
                return Mono.empty();
            }
        };
        DefaultClientCredentialGranter legacy = new DefaultClientCredentialGranter(
                authenticationManager,
                accessTokenManager,
                event -> {
                });
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client");
        client.setUserId("user-id");

        StepVerifier
                .create(legacy.requestToken(new ClientCredentialRequest(client, Collections.emptyMap())))
                .expectNext(expectedToken)
                .verifyComplete();

        assertEquals(1, managerCalls.get());
        assertEquals("user-id", requestedUserId.get());
        assertEquals(1, createCalls.get());
        assertEquals("client", requestedClientId.get());
        assertSame(expectedAuthentication, tokenAuthentication.get());
        assertEquals(Boolean.TRUE, singleton.get());
    }

    private static AnnotationConfigReactiveWebApplicationContext contextWithInfrastructure() {
        AnnotationConfigReactiveWebApplicationContext context =
                new AnnotationConfigReactiveWebApplicationContext();
        context.registerBean("accessTokenManager", AccessTokenManager.class, StubAccessTokenManager::new);
        context.registerBean("authorizationCodeGranter", AuthorizationCodeGranter.class, StubAuthorizationCodeGranter::new);
        context.register(OAuth2ServerAutoConfiguration.class);
        return context;
    }

    private static void refresh(AnnotationConfigReactiveWebApplicationContext context) {
        context.refresh();
    }

    private static ClientCredentialGrantHandler handler(String clientType, AccessToken token) {
        return new ClientCredentialGrantHandler() {
            @Override
            public String getClientType() {
                return clientType;
            }

            @Override
            public Mono<AccessToken> requestToken(ClientCredentialRequest request) {
                return Mono.just(token);
            }
        };
    }

    private static ClientCredentialRequest request(String clientType) {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client");
        return new ClientCredentialRequest(
                new OAuth2ClientAuthentication(client, clientType, Collections.emptyMap()),
                Collections.emptyMap());
    }

    private static boolean hasMessage(Throwable error, String expected) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private static final class StubAccessTokenManager implements AccessTokenManager {
        @Override
        public Mono<Authentication> getAuthenticationByToken(String accessToken) {
            return Mono.empty();
        }

        @Override
        public Mono<AccessToken> createAccessToken(String clientId, Authentication authentication, boolean singleton) {
            return Mono.empty();
        }

        @Override
        public Mono<AccessToken> refreshAccessToken(String clientId, String refreshToken) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> removeToken(String clientId, String token) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> cancelGrant(String clientId, String userId) {
            return Mono.empty();
        }
    }

    private static final class StubAuthorizationCodeGranter implements AuthorizationCodeGranter {
        @Override
        public Mono<AuthorizationCodeResponse> requestCode(AuthorizationCodeRequest request) {
            return Mono.empty();
        }

        @Override
        public Mono<AccessToken> requestToken(AuthorizationCodeTokenRequest request) {
            return Mono.empty();
        }
    }

}
