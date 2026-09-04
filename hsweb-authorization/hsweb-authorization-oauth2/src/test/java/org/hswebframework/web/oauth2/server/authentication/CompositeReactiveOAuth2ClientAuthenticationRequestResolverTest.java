package org.hswebframework.web.oauth2.server.authentication;

import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CompositeReactiveOAuth2ClientAuthenticationRequestResolverTest {

    @Test
    public void shouldUseFirstRecognizedConverterSequentially() {
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();
        OAuth2ClientAuthenticationRequest expected = request("custom");
        ReactiveOAuth2ClientAuthenticationRequestConverter first = (exchange, parameters, grantType) -> {
            firstCalls.incrementAndGet();
            return Mono.empty();
        };
        ReactiveOAuth2ClientAuthenticationRequestConverter second =
            (exchange, parameters, grantType) -> Mono.just(expected);
        ReactiveOAuth2ClientAuthenticationRequestConverter fallback = (exchange, parameters, grantType) -> {
            fallbackCalls.incrementAndGet();
            return Mono.just(request("fallback"));
        };
        CompositeReactiveOAuth2ClientAuthenticationRequestResolver resolver =
            new CompositeReactiveOAuth2ClientAuthenticationRequestResolver(
                Arrays.asList(first, second), fallback);

        resolver
            .resolve(exchange(), new LinkedMultiValueMap<>(), "client_credentials")
            .as(StepVerifier::create)
            .assertNext(actual -> assertSame(expected, actual))
            .verifyComplete();

        assertEquals(1, firstCalls.get());
        assertEquals(0, fallbackCalls.get());
    }

    @Test
    public void shouldNotFallbackAfterRecognizedError() {
        RuntimeException expected = new RuntimeException("malformed evidence");
        AtomicInteger fallbackCalls = new AtomicInteger();
        ReactiveOAuth2ClientAuthenticationRequestConverter failing =
            (exchange, parameters, grantType) -> Mono.error(expected);
        ReactiveOAuth2ClientAuthenticationRequestConverter fallback = (exchange, parameters, grantType) -> {
            fallbackCalls.incrementAndGet();
            return Mono.just(request("fallback"));
        };
        CompositeReactiveOAuth2ClientAuthenticationRequestResolver resolver =
            new CompositeReactiveOAuth2ClientAuthenticationRequestResolver(
                Collections.singletonList(failing), fallback);

        resolver
            .resolve(exchange(), new LinkedMultiValueMap<>(), "client_credentials")
            .as(StepVerifier::create)
            .verifyErrorSatisfies(error -> assertSame(expected, error));

        assertEquals(0, fallbackCalls.get());
    }

    private OAuth2ClientAuthenticationRequest request(String method) {
        return new OAuth2ClientAuthenticationRequest(
            "client",
            method,
            "client_credentials",
            Collections.emptyMap()) {
        };
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/oauth2/token"));
    }
}
