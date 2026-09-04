package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompositeClientCredentialGranterTest {

    @Test
    public void shouldRouteOnlyByAuthenticatedClientType() {
        AccessToken expected = new AccessToken("api-token", null, 60);
        AtomicInteger defaultCalls = new AtomicInteger();

        CompositeClientCredentialGranter granter = new CompositeClientCredentialGranter(Arrays.asList(
                handler(OAuth2Client.DEFAULT_CLIENT_TYPE, request -> {
                    defaultCalls.incrementAndGet();
                    return Mono.error(new AssertionError("default handler must not be used"));
                }),
                handler("api", request -> Mono.just(expected))
        ));

        StepVerifier
                .create(granter.requestToken(request("api")))
                .expectNext(expected)
                .verifyComplete();

        assertEquals(0, defaultCalls.get());
    }

    @Test
    public void shouldPropagateSelectedHandlerErrorWithoutFallback() {
        RuntimeException expected = new RuntimeException("issue token failed");
        AtomicInteger defaultCalls = new AtomicInteger();

        CompositeClientCredentialGranter granter = new CompositeClientCredentialGranter(Arrays.asList(
                handler(OAuth2Client.DEFAULT_CLIENT_TYPE, request -> {
                    defaultCalls.incrementAndGet();
                    return Mono.empty();
                }),
                handler("api", request -> Mono.error(expected))
        ));

        StepVerifier
                .create(granter.requestToken(request("api")))
                .verifyErrorSatisfies(error -> assertSame(expected, error));

        assertEquals(0, defaultCalls.get());
    }

    @Test
    public void shouldRejectUnknownClientTypeAsUnauthorizedClient() {
        CompositeClientCredentialGranter granter = new CompositeClientCredentialGranter(
                Collections.singletonList(handler(
                        OAuth2Client.DEFAULT_CLIENT_TYPE,
                        request -> Mono.empty())));

        StepVerifier
                .create(granter.requestToken(request("unknown")))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof OAuth2Exception);
                    assertSame(ErrorType.UNAUTHORIZED_CLIENT, ((OAuth2Exception) error).getType());
                });
    }

    @Test
    public void shouldRejectBlankAndDuplicateHandlerTypes() {
        assertConstructionFails(
                Collections.singletonList(handler(" ", request -> Mono.empty())),
                IllegalArgumentException.class,
                "must not be blank");

        assertConstructionFails(
                Arrays.asList(
                        handler("api", request -> Mono.empty()),
                        handler("api", request -> Mono.empty())),
                IllegalStateException.class,
                "duplicate client credential handler type: api");
    }

    private static ClientCredentialRequest request(String clientType) {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client");
        return new ClientCredentialRequest(
                new OAuth2ClientAuthentication(client, clientType, Collections.emptyMap()),
                Collections.emptyMap());
    }

    private static ClientCredentialGrantHandler handler(
            String clientType,
            java.util.function.Function<ClientCredentialRequest, Mono<AccessToken>> issuer) {
        return new ClientCredentialGrantHandler() {
            @Override
            public String getClientType() {
                return clientType;
            }

            @Override
            public Mono<AccessToken> requestToken(ClientCredentialRequest request) {
                return issuer.apply(request);
            }
        };
    }

    private static void assertConstructionFails(
            java.util.Collection<ClientCredentialGrantHandler> handlers,
            Class<? extends RuntimeException> expectedType,
            String expectedMessage) {
        try {
            new CompositeClientCredentialGranter(handlers);
            fail("expected construction to fail");
        } catch (RuntimeException error) {
            assertSame(expectedType, error.getClass());
            assertTrue(error.getMessage().contains(expectedMessage));
        }
    }
}
