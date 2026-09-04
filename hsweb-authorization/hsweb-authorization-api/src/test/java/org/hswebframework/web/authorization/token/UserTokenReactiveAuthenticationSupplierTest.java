package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationRequest;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserTokenReactiveAuthenticationSupplierTest {

    @Test
    public void shouldAuthenticateUsingMatchedProvider() {
        CountingUserTokenManager tokenManager = new CountingUserTokenManager();
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(tokenManager);
        Authentication authentication = authentication("api-application");
        ParsedToken parsedToken = ParsedToken.of("api", "ak_test");
        AtomicReference<ParsedToken> received = new AtomicReference<>();

        supplier.setTokenAuthenticationProviders(Collections.singletonList(provider("api", token -> {
            received.set(token);
            return Mono.just(authentication);
        })));

        StepVerifier
            .create(supplier.get().contextWrite(Context.of(ParsedToken.class, parsedToken)))
            .assertNext(actual -> assertSame(authentication, actual))
            .verifyComplete();

        assertSame(parsedToken, received.get());
        assertEquals(0, tokenManager.lookupCount.get());
    }

    @Test
    public void shouldNotFallbackWhenMatchedProviderReturnsEmpty() {
        CountingUserTokenManager tokenManager = new CountingUserTokenManager();
        tokenManager.userToken = validUserToken("legacy", "legacy-user", "default");
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(tokenManager);

        supplier.setTokenAuthenticationProviders(
            Collections.singletonList(provider("api", ignore -> Mono.empty())));

        StepVerifier
            .create(supplier
                        .get()
                        .contextWrite(Context.of(ParsedToken.class, ParsedToken.of("api", "ak_invalid"))))
            .verifyComplete();

        assertEquals(0, tokenManager.lookupCount.get());
        assertEquals(0, tokenManager.touchCount.get());
    }

    @Test
    public void shouldPropagateMatchedProviderErrorWithoutFallback() {
        CountingUserTokenManager tokenManager = new CountingUserTokenManager();
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(tokenManager);
        IllegalStateException failure = new IllegalStateException("authentication failed");

        supplier.setTokenAuthenticationProviders(
            Collections.singletonList(provider("api", ignore -> Mono.error(failure))));

        StepVerifier
            .create(supplier
                        .get()
                        .contextWrite(Context.of(ParsedToken.class, ParsedToken.of("api", "ak_invalid"))))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertEquals(0, tokenManager.lookupCount.get());
        assertEquals(0, tokenManager.touchCount.get());
    }

    @Test
    public void shouldKeepLegacyThirdPartAuthenticationFlow() {
        CountingUserTokenManager tokenManager = new CountingUserTokenManager();
        tokenManager.userToken = validUserToken("legacy-token", "legacy-user", "third-party");
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(tokenManager);
        Authentication authentication = authentication("legacy-user");
        AtomicInteger thirdPartLookupCount = new AtomicInteger();

        supplier.setTokenAuthenticationProviders(
            Collections.singletonList(provider("api", ignore -> Mono.empty())));
        supplier.setThirdPartAuthenticationManager(Collections.singletonList(
            new ThirdPartReactiveAuthenticationManager() {
                @Override
                public String getTokenType() {
                    return "third-party";
                }

                @Override
                public Mono<Authentication> getByUserId(String userId) {
                    assertEquals("legacy-user", userId);
                    thirdPartLookupCount.incrementAndGet();
                    return Mono.just(authentication);
                }
            }));

        StepVerifier
            .create(supplier
                        .get()
                        .contextWrite(Context.of(
                            ParsedToken.class,
                            ParsedToken.of("bearer", "legacy-token"))))
            .assertNext(actual -> assertSame(authentication, actual))
            .verifyComplete();

        assertEquals(1, tokenManager.lookupCount.get());
        assertEquals(1, tokenManager.touchCount.get());
        assertEquals(1, thirdPartLookupCount.get());
    }

    @Test
    public void shouldRejectDuplicateProviderType() {
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(new CountingUserTokenManager());
        ReactiveTokenAuthenticationProvider first = provider("api", ignore -> Mono.empty());
        ReactiveTokenAuthenticationProvider second = provider("api", ignore -> Mono.empty());

        try {
            supplier.setTokenAuthenticationProviders(Arrays.asList(first, second));
            fail("duplicate provider type must be rejected");
        } catch (IllegalStateException error) {
            assertTrue(error.getMessage().contains("api"));
        }
    }

    @Test
    public void shouldRejectEmptyProviderType() {
        UserTokenReactiveAuthenticationSupplier supplier = createSupplier(new CountingUserTokenManager());

        try {
            supplier.setTokenAuthenticationProviders(
                Collections.singletonList(provider(" ", ignore -> Mono.empty())));
            fail("empty provider type must be rejected");
        } catch (IllegalArgumentException error) {
            assertTrue(error.getMessage().contains("tokenType"));
        }
    }

    private static UserTokenReactiveAuthenticationSupplier createSupplier(UserTokenManager tokenManager) {
        return new UserTokenReactiveAuthenticationSupplier(tokenManager, new ReactiveAuthenticationManager() {
            @Override
            public Mono<Authentication> authenticate(Mono<AuthenticationRequest> request) {
                return Mono.empty();
            }

            @Override
            public Mono<Authentication> getByUserId(String userId) {
                return Mono.empty();
            }
        });
    }

    private static ReactiveTokenAuthenticationProvider provider(
        String tokenType,
        Function<ParsedToken, Mono<Authentication>> authenticator) {
        return new ReactiveTokenAuthenticationProvider() {
            @Override
            public String getTokenType() {
                return tokenType;
            }

            @Override
            public Mono<Authentication> authenticate(ParsedToken token) {
                return authenticator.apply(token);
            }
        };
    }

    private static Authentication authentication(String userId) {
        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser.builder().id(userId).build());
        return authentication;
    }

    private static LocalUserToken validUserToken(String token, String userId, String tokenType) {
        LocalUserToken userToken = new LocalUserToken(userId, token);
        userToken.setType(tokenType);
        userToken.setState(TokenState.normal);
        userToken.setMaxInactiveInterval(-1);
        return userToken;
    }

    private static class CountingUserTokenManager extends DefaultUserTokenManager {

        private final AtomicInteger lookupCount = new AtomicInteger();

        private final AtomicInteger touchCount = new AtomicInteger();

        private UserToken userToken;

        @Override
        public Mono<UserToken> getByToken(String token) {
            lookupCount.incrementAndGet();
            return Mono.justOrEmpty(userToken);
        }

        @Override
        public Mono<Void> touch(String token) {
            touchCount.incrementAndGet();
            return Mono.empty();
        }
    }
}
