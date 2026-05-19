package org.hswebframework.web.oauth2.server.code;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultAuthorizationCodeGranterRedirectUriTest {

    @Test
    public void shouldRequestTokenWithSameRedirectUri() {
        DefaultAuthorizationCodeGranter granter = createGranter();
        OAuth2Client client = createClient();

        Map<String, String> request = Collections.singletonMap("redirect_uri", "http://hsweb.me/callback/next");

        granter
                .requestCode(new AuthorizationCodeRequest(client, createAuthentication(), request))
                .flatMap(response -> granter.requestToken(new AuthorizationCodeTokenRequest(client, createTokenRequest(response.getCode(), "http://hsweb.me/callback/next"))))
                .as(StepVerifier::create)
                .expectNextMatches(token -> "access-token".equals(token.getAccessToken()))
                .verifyComplete();
    }

    @Test
    public void shouldRejectTokenRequestWhenRedirectUriDoesNotMatchAuthorizedUri() {
        DefaultAuthorizationCodeGranter granter = createGranter();
        OAuth2Client client = createClient();

        Map<String, String> request = Collections.singletonMap("redirect_uri", "http://hsweb.me/callback/next");

        granter
                .requestCode(new AuthorizationCodeRequest(client, createAuthentication(), request))
                .flatMap(response -> granter.requestToken(new AuthorizationCodeTokenRequest(client, Collections.singletonMap("code", response.getCode()))))
                .as(StepVerifier::create)
                .expectErrorMatches(error -> error instanceof OAuth2Exception
                        && ((OAuth2Exception) error).getType() == ErrorType.ILLEGAL_REDIRECT_URI)
                .verify();
    }

    private DefaultAuthorizationCodeGranter createGranter() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        return new DefaultAuthorizationCodeGranter(
                new TestAccessTokenManager(),
                context,
                createRedisOperations()
        );
    }

    private OAuth2Client createClient() {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("test-client");
        client.setClientSecret("test-secret");
        client.setRedirectUrl("http://hsweb.me/callback");
        return client;
    }

    private Authentication createAuthentication() {
        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser
                                       .builder()
                                       .id("test-user")
                                       .build());
        return authentication;
    }

    private Map<String, String> createTokenRequest(String code, String redirectUri) {
        Map<String, String> request = new HashMap<>();
        request.put("code", code);
        request.put("redirect_uri", redirectUri);
        return request;
    }

    @SuppressWarnings("unchecked")
    private ReactiveRedisOperations<String, AuthorizationCodeCache> createRedisOperations() {
        Map<String, AuthorizationCodeCache> storage = new HashMap<>();

        ReactiveValueOperations<String, AuthorizationCodeCache> valueOperations =
                (ReactiveValueOperations<String, AuthorizationCodeCache>) Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{ReactiveValueOperations.class},
                        (proxy, method, args) -> {
                            if (isObjectMethod(method)) {
                                return handleObjectMethod(proxy, method, args, "InMemoryReactiveValueOperations");
                            }
                            if ("set".equals(method.getName())) {
                                storage.put((String) args[0], (AuthorizationCodeCache) args[1]);
                                return Mono.just(true);
                            }
                            if ("get".equals(method.getName())) {
                                return Mono.justOrEmpty(storage.get((String) args[0]));
                            }
                            if ("delete".equals(method.getName())) {
                                return Mono.just(storage.remove((String) args[0]) != null);
                            }
                            throw new UnsupportedOperationException(method.toGenericString());
                        }
                );

        return (ReactiveRedisOperations<String, AuthorizationCodeCache>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ReactiveRedisOperations.class},
                (proxy, method, args) -> {
                    if (isObjectMethod(method)) {
                        return handleObjectMethod(proxy, method, args, "InMemoryReactiveRedisOperations");
                    }
                    if ("opsForValue".equals(method.getName())) {
                        return valueOperations;
                    }
                    throw new UnsupportedOperationException(method.toGenericString());
                }
        );
    }

    private boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private Object handleObjectMethod(Object proxy, Method method, Object[] args, String name) {
        if ("toString".equals(method.getName())) {
            return name;
        }
        if ("hashCode".equals(method.getName())) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        return null;
    }

    private static class TestAccessTokenManager implements AccessTokenManager {

        @Override
        public Mono<Authentication> getAuthenticationByToken(String accessToken) {
            return Mono.empty();
        }

        @Override
        public Mono<AccessToken> createAccessToken(String clientId,
                                                   Authentication authentication,
                                                   boolean singleton) {
            return Mono.just(new AccessToken("access-token", "refresh-token", 7200));
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
}
