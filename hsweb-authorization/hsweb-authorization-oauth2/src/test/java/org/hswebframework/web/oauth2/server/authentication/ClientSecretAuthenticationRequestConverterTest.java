package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class ClientSecretAuthenticationRequestConverterTest {

    private final ClientSecretAuthenticationRequestConverter converter =
        new ClientSecretAuthenticationRequestConverter();

    @Test
    public void shouldResolveBasicBeforeFormAndPreserveColonInSecret() {
        MultiValueMap<String, String> parameters = parameters();
        parameters.add("client_id", "ignored-client");
        parameters.add("client_secret", "ignored-secret");
        parameters.add("scope", "read");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest
                .get("/oauth2/token")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + java.util.Base64.getEncoder().encodeToString(
                        "client:secret:with:colon".getBytes(StandardCharsets.UTF_8))));
        AtomicReference<OAuth2ClientSecretAuthenticationRequest> resolved = new AtomicReference<>();

        converter
            .convert(exchange, parameters, "client_credentials")
            .cast(OAuth2ClientSecretAuthenticationRequest.class)
            .doOnNext(resolved::set)
            .as(StepVerifier::create)
            .assertNext(request -> {
                assertEquals("client", request.getClientId());
                assertEquals(OAuth2ClientAuthenticationRequest.CLIENT_SECRET_BASIC,
                             request.getAuthenticationMethod());
                assertArrayEquals("secret:with:colon".toCharArray(), request.getClientSecret());
                assertEquals("read", request.getParameters().get("scope"));
                assertFalse(request.getParameters().containsKey("client_secret"));
            })
            .verifyComplete();

        resolved.get().eraseCredentials();
        assertNull(resolved.get().getClientSecret());
    }

    @Test
    public void shouldRejectDuplicateFormCredentials() {
        MultiValueMap<String, String> duplicateClientId = parameters();
        duplicateClientId.add("client_id", "one");
        duplicateClientId.add("client_id", "two");
        duplicateClientId.add("client_secret", "secret");

        converter
            .convert(exchange(), duplicateClientId, "client_credentials")
            .as(StepVerifier::create)
            .expectErrorMatches(error -> hasType(error, ErrorType.ILLEGAL_CLIENT_ID))
            .verify();

        MultiValueMap<String, String> duplicateSecret = parameters();
        duplicateSecret.add("client_id", "client");
        duplicateSecret.add("client_secret", "one");
        duplicateSecret.add("client_secret", "two");

        converter
            .convert(exchange(), duplicateSecret, "client_credentials")
            .as(StepVerifier::create)
            .expectErrorMatches(error -> hasType(error, ErrorType.ILLEGAL_CLIENT_SECRET))
            .verify();
    }

    private MultiValueMap<String, String> parameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "client_credentials");
        return parameters;
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/token"));
    }

    private boolean hasType(Throwable error, ErrorType type) {
        return error instanceof OAuth2Exception && ((OAuth2Exception) error).getType() == type;
    }
}
