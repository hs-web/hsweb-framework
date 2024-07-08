package org.hswebframework.web.oauth2.server.impl;

import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.oauth2.server.RedisHelper;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

@Ignore
public class RedisAccessTokenManagerTest {

    @Test
    public void testCreateAccessToken() {
        RedisAccessTokenManager tokenManager = new RedisAccessTokenManager(RedisHelper.factory);

        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser.builder()
                                         .id("test")
                                         .build());
        tokenManager.createAccessToken("test", authentication, false)
                    .doOnNext(System.out::println)
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete();

    }

    @Test
    public void testRefreshToken() {
        RedisAccessTokenManager tokenManager = new RedisAccessTokenManager(RedisHelper.factory);

        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser.builder()
                                         .id("test")
                                         .build());
        tokenManager
            .createAccessToken("test", authentication, false)
            .zipWhen(token -> tokenManager.refreshAccessToken("test", token.getRefreshToken()))
            .as(StepVerifier::create)
            .expectNextMatches(tp2 -> {
                return tp2.getT1().getRefreshToken().equals(tp2.getT2().getRefreshToken());
            })
        ;

    }

    @Test
    public void testCreateSingletonAccessToken() {
        RedisAccessTokenManager tokenManager = new RedisAccessTokenManager(RedisHelper.factory);

        SimpleAuthentication authentication = new SimpleAuthentication();
        authentication.setUser(SimpleUser.builder()
                                         .id("test")
                                         .build());
        Flux
            .concat(tokenManager
                        .createAccessToken("test", authentication, true),
                    tokenManager
                        .createAccessToken("test", authentication, true))
            .doOnNext(System.out::println)
            .as(StepVerifier::create)
            .expectNextCount(2)
            .verifyComplete();

    }
}