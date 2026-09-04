package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompositeReactiveOAuth2ClientAuthenticatorTest {

    @Test
    public void shouldUseCustomProviderInsteadOfFallbackForSameMethod() {
        OAuth2ClientAuthentication expected = authentication();
        AtomicInteger fallbackCalls = new AtomicInteger();
        ReactiveOAuth2ClientAuthenticationProvider custom = provider(
            Collections.singleton("custom"), request -> Mono.just(expected));
        ReactiveOAuth2ClientAuthenticationProvider fallback = provider(
            Arrays.asList("custom", "default"), request -> {
                fallbackCalls.incrementAndGet();
                return Mono.error(new AssertionError("custom provider must win"));
            });
        CompositeReactiveOAuth2ClientAuthenticator authenticator =
            new CompositeReactiveOAuth2ClientAuthenticator(
                Collections.singletonList(custom), fallback);

        authenticator
            .authenticate(request("custom"))
            .as(StepVerifier::create)
            .assertNext(actual -> assertSame(expected, actual))
            .verifyComplete();

        assertEquals(0, fallbackCalls.get());
    }

    @Test
    public void shouldRejectDuplicateCustomMethodsAtConstruction() {
        ReactiveOAuth2ClientAuthenticationProvider first =
            provider(Collections.singleton("custom"), request -> Mono.just(authentication()));
        ReactiveOAuth2ClientAuthenticationProvider second =
            provider(Collections.singleton("custom"), request -> Mono.just(authentication()));

        try {
            new CompositeReactiveOAuth2ClientAuthenticator(
                Arrays.asList(first, second),
                provider(Collections.singleton("default"), request -> Mono.just(authentication())));
            fail("duplicate custom methods must fail");
        } catch (IllegalStateException error) {
            assertTrue(error.getMessage().contains("duplicate oauth2 client authentication provider method"));
        }
    }

    @Test
    public void shouldNotFallbackAfterProviderFailureOrEmptyCompletion() {
        RuntimeException expected = new RuntimeException("rejected");
        ReactiveOAuth2ClientAuthenticationProvider failing =
            provider(Collections.singleton("failing"), request -> Mono.error(expected));
        ReactiveOAuth2ClientAuthenticationProvider empty =
            provider(Collections.singleton("empty"), request -> Mono.empty());
        CompositeReactiveOAuth2ClientAuthenticator authenticator =
            new CompositeReactiveOAuth2ClientAuthenticator(
                Arrays.asList(failing, empty),
                provider(Collections.singleton("default"), request -> Mono.just(authentication())));

        authenticator
            .authenticate(request("failing"))
            .as(StepVerifier::create)
            .verifyErrorSatisfies(error -> assertSame(expected, error));

        authenticator
            .authenticate(request("empty"))
            .as(StepVerifier::create)
            .expectErrorMatches(error -> hasType(error, ErrorType.UNAUTHORIZED_CLIENT))
            .verify();

        authenticator
            .authenticate(request("unknown"))
            .as(StepVerifier::create)
            .expectErrorMatches(error -> hasType(error, ErrorType.ILLEGAL_AUTHORIZATION))
            .verify();
    }

    private ReactiveOAuth2ClientAuthenticationProvider provider(
        Collection<String> methods,
        java.util.function.Function<OAuth2ClientAuthenticationRequest,
            Mono<OAuth2ClientAuthentication>> authenticator) {
        return new ReactiveOAuth2ClientAuthenticationProvider() {
            @Override
            public Collection<String> getAuthenticationMethods() {
                return methods;
            }

            @Override
            public Mono<OAuth2ClientAuthentication> authenticate(
                OAuth2ClientAuthenticationRequest request) {
                return authenticator.apply(request);
            }
        };
    }

    private OAuth2ClientAuthenticationRequest request(String method) {
        return new OAuth2ClientAuthenticationRequest(
            "client",
            method,
            "client_credentials",
            Collections.emptyMap()) {
        };
    }

    private OAuth2ClientAuthentication authentication() {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client");
        return new OAuth2ClientAuthentication(client);
    }

    private boolean hasType(Throwable error, ErrorType type) {
        return error instanceof OAuth2Exception && ((OAuth2Exception) error).getType() == type;
    }
}
