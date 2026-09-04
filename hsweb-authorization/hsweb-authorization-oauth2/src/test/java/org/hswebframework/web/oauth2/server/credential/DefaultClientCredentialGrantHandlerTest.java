package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DefaultClientCredentialGrantHandlerTest {

    @Test
    public void shouldExposeDefaultTypeAndDelegateOriginalRequestAndSuccessPublisher() {
        ClientCredentialRequest request = request();
        AccessToken expected = new AccessToken("token", null, 60);
        Mono<AccessToken> expectedPublisher = Mono.just(expected);
        AtomicReference<ClientCredentialRequest> delegatedRequest = new AtomicReference<>();
        ClientCredentialGranter delegate = actual -> {
            delegatedRequest.set(actual);
            return expectedPublisher;
        };
        DefaultClientCredentialGrantHandler handler =
                new DefaultClientCredentialGrantHandler(delegate);

        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, handler.getClientType());
        Mono<AccessToken> actualPublisher = handler.requestToken(request);
        assertSame(request, delegatedRequest.get());
        assertSame(expectedPublisher, actualPublisher);

        StepVerifier
                .create(actualPublisher)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    public void shouldPropagateDelegateErrorPublisherWithoutFallback() {
        ClientCredentialRequest request = request();
        RuntimeException expected = new RuntimeException("token issue failed");
        Mono<AccessToken> expectedPublisher = Mono.error(expected);
        AtomicReference<ClientCredentialRequest> delegatedRequest = new AtomicReference<>();
        DefaultClientCredentialGrantHandler handler = new DefaultClientCredentialGrantHandler(actual -> {
            delegatedRequest.set(actual);
            return expectedPublisher;
        });

        Mono<AccessToken> actualPublisher = handler.requestToken(request);
        assertSame(request, delegatedRequest.get());
        assertSame(expectedPublisher, actualPublisher);

        StepVerifier
                .create(actualPublisher)
                .verifyErrorSatisfies(error -> assertSame(expected, error));
    }

    private static ClientCredentialRequest request() {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client");
        return new ClientCredentialRequest(client, Collections.emptyMap());
    }
}
