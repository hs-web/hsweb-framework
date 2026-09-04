package org.hswebframework.web.oauth2.server.web;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2GrantService;
import org.hswebframework.web.oauth2.server.OAuth2Properties;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthenticationRequest;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientSecretAuthenticationRequest;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeRequest;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeResponse;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeTokenRequest;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialRequest;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenGranter;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class OAuth2AuthorizeControllerTest {

    @Test
    public void testBuildRedirect() {
        String url = OAuth2AuthorizeController.buildRedirect(
            "http://hsweb.me/callback",
            Collections.singletonMap("code", "1234"));

        assertEquals("http://hsweb.me/callback?code=1234", url);
    }

    @Test
    public void testBuildRedirectParam() {
        String url = OAuth2AuthorizeController.buildRedirect(
            "http://hsweb.me/callback?a=b",
            Collections.singletonMap("code", "1234"));

        assertEquals("http://hsweb.me/callback?a=b&code=1234", url);
    }

    @Test
    public void shouldClearCredentialsOnEveryAuthenticationTermination() {
        AtomicReference<OAuth2ClientAuthenticationRequest> completedRequest = new AtomicReference<>();
        OAuth2AuthorizeController completedController = controller(request -> {
            assertEquals("secret", new String(clientSecret(request)));
            completedRequest.set(request);
            OAuth2Client client = new OAuth2Client();
            client.setClientId(request.getClientId());
            return Mono.just(new OAuth2ClientAuthentication(client));
        });
        StepVerifier
            .create(requestClientCredentialToken(completedController))
            .expectNextCount(1)
            .verifyComplete();
        awaitCredentialsCleared(completedRequest.get());

        RuntimeException asyncError = new RuntimeException("async authentication failed");
        AtomicReference<OAuth2ClientAuthenticationRequest> asyncRequest = new AtomicReference<>();
        OAuth2AuthorizeController asyncController = controller(request -> {
            asyncRequest.set(request);
            return Mono
                .delay(Duration.ofMillis(1))
                .flatMap(ignore -> {
                    assertEquals("secret", new String(clientSecret(request)));
                    return Mono.error(asyncError);
                });
        });
        StepVerifier
            .create(requestClientCredentialToken(asyncController))
            .verifyErrorSatisfies(error -> assertSame(asyncError, error));
        awaitCredentialsCleared(asyncRequest.get());

        AtomicReference<OAuth2ClientAuthenticationRequest> cancelledRequest = new AtomicReference<>();
        OAuth2AuthorizeController cancelledController = controller(request -> {
            assertEquals("secret", new String(clientSecret(request)));
            cancelledRequest.set(request);
            return Mono.never();
        });
        StepVerifier
            .create(requestClientCredentialToken(cancelledController))
            .then(() -> assertNotNull(cancelledRequest.get()))
            .thenCancel()
            .verify();
        awaitCredentialsCleared(cancelledRequest.get());

        RuntimeException syncError = new RuntimeException("synchronous authentication failed");
        AtomicReference<OAuth2ClientAuthenticationRequest> syncRequest = new AtomicReference<>();
        OAuth2AuthorizeController syncController = controller(request -> {
            assertEquals("secret", new String(clientSecret(request)));
            syncRequest.set(request);
            throw syncError;
        });
        StepVerifier
            .create(requestClientCredentialToken(syncController))
            .verifyErrorSatisfies(error -> assertSame(syncError, error));
        awaitCredentialsCleared(syncRequest.get());
    }

    @Test
    public void shouldUseSanitizedClientForAuthorizationCodeGrant() {
        assertGrantUsesSanitizedClient(OAuth2AuthorizeController.GrantType.authorization_code);
    }

    @Test
    public void shouldUseSanitizedClientForRefreshTokenGrant() {
        assertGrantUsesSanitizedClient(OAuth2AuthorizeController.GrantType.refresh_token);
    }

    private void assertGrantUsesSanitizedClient(OAuth2AuthorizeController.GrantType grantType) {
        OAuth2Client source = new OAuth2Client();
        source.setClientId("client-id");
        source.setClientSecret("secret");
        source.setName("client name");
        source.setDescription("client description");
        source.setRedirectUrl("https://example.com/callback");
        source.setUserId("user-id");
        OAuth2ClientAuthentication authentication =
            new OAuth2ClientAuthentication(source, "api", Collections.emptyMap());
        AccessToken token = new AccessToken("token", null, 60);
        AtomicReference<OAuth2Client> downstreamClient = new AtomicReference<>();
        AtomicReference<Map<String, String>> downstreamParameters = new AtomicReference<>();

        OAuth2GrantService grantService = new OAuth2GrantService() {
            @Override
            public AuthorizationCodeGranter authorizationCode() {
                return new AuthorizationCodeGranter() {
                    @Override
                    public Mono<AuthorizationCodeResponse> requestCode(AuthorizationCodeRequest request) {
                        return Mono.empty();
                    }

                    @Override
                    public Mono<AccessToken> requestToken(AuthorizationCodeTokenRequest request) {
                        downstreamClient.set(request.getClient());
                        downstreamParameters.set(request.getParameters());
                        return Mono.just(token);
                    }
                };
            }

            @Override
            public ClientCredentialGranter clientCredential() {
                return request -> Mono.error(new AssertionError("unexpected client credentials grant"));
            }

            @Override
            public RefreshTokenGranter refreshToken() {
                return request -> {
                    downstreamClient.set(request.getClient());
                    downstreamParameters.set(request.getParameters());
                    return Mono.just(token);
                };
            }
        };
        OAuth2AuthorizeController controller = new OAuth2AuthorizeController(
            grantService,
            id -> Mono.error(new AssertionError("token endpoint must use the authenticator")),
            new OAuth2Properties(),
            request -> Mono.just(authentication));
        MultiValueMap<String, String> query = tokenQuery(grantType.name());
        query.add("client_id", "client-id");
        query.add("client_secret", "secret");
        query.add("code", "code");
        query.add("refresh_token", "refresh-token");

        controller
            .requestTokenByCode(grantType, exchange(query))
            .as(StepVerifier::create)
            .expectNext(ResponseEntity.ok(token))
            .verifyComplete();

        assertSame(authentication.getClient(), downstreamClient.get());
        assertNotSame(source, downstreamClient.get());
        assertNull(downstreamClient.get().getClientSecret());
        assertEquals("client-id", downstreamClient.get().getClientId());
        assertEquals("client name", downstreamClient.get().getName());
        assertEquals("client description", downstreamClient.get().getDescription());
        assertEquals("https://example.com/callback", downstreamClient.get().getRedirectUrl());
        assertEquals("user-id", downstreamClient.get().getUserId());
        assertEquals("secret", source.getClientSecret());
        assertFalse(downstreamParameters.get().containsKey("client_secret"));
    }

    private Mono<ResponseEntity<AccessToken>> requestClientCredentialToken(
        OAuth2AuthorizeController controller) {
        MultiValueMap<String, String> query = tokenQuery("client_credentials");
        query.add("client_id", "client-id");
        query.add("client_secret", "secret");
        return controller.requestTokenByCode(
            OAuth2AuthorizeController.GrantType.client_credentials,
            exchange(query));
    }

    private OAuth2AuthorizeController controller(ReactiveOAuth2ClientAuthenticator authenticator) {
        return new OAuth2AuthorizeController(
            grantService(new AtomicReference<>(), new AccessToken("token", null, 60)),
            id -> Mono.error(new AssertionError("token endpoint must use the authenticator")),
            new OAuth2Properties(),
            authenticator);
    }

    private MultiValueMap<String, String> tokenQuery(String grantType) {
        MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        query.add("grant_type", grantType);
        return query;
    }

    private void awaitCredentialsCleared(OAuth2ClientAuthenticationRequest request) {
        assertNull(((OAuth2ClientSecretAuthenticationRequest) request).getClientSecret());
    }

    private char[] clientSecret(OAuth2ClientAuthenticationRequest request) {
        return ((OAuth2ClientSecretAuthenticationRequest) request).getClientSecret();
    }

    private ServerWebExchange exchange(MultiValueMap<String, String> query) {
        return MockServerWebExchange.from(
            MockServerHttpRequest
                .get("/oauth2/token")
                .queryParams(query));
    }

    private OAuth2GrantService grantService(AtomicReference<ClientCredentialRequest> grantRequest,
                                            AccessToken token) {
        ClientCredentialGranter clientCredentialGranter = request -> {
            grantRequest.set(request);
            return Mono.just(token);
        };
        return new OAuth2GrantService() {
            @Override
            public AuthorizationCodeGranter authorizationCode() {
                return null;
            }

            @Override
            public ClientCredentialGranter clientCredential() {
                return clientCredentialGranter;
            }

            @Override
            public RefreshTokenGranter refreshToken() {
                return null;
            }
        };
    }
}
