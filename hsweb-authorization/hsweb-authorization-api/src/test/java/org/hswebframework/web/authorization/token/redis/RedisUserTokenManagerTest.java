package org.hswebframework.web.authorization.token.redis;

import lombok.SneakyThrows;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.AllopatricLoginMode;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;

import static org.junit.Assert.*;

public class RedisUserTokenManagerTest {

    UserTokenManager tokenManager;

    @Before
    public void init() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1"));

        ReactiveRedisTemplate<Object, Object> template = new ReactiveRedisTemplate<>(
                factory,
                RedisSerializationContext.java()
        );
        factory.afterPropertiesSet();

        RedisUserTokenManager tokenManager = new RedisUserTokenManager(template);
        this.tokenManager = tokenManager;
        tokenManager.setAllopatricLoginModes(new HashMap<String, AllopatricLoginMode>() {
            {
                put("offline", AllopatricLoginMode.offlineOther);
                put("deny", AllopatricLoginMode.deny);
            }
        });
    }

    @Test
    public void testSign() {

        tokenManager.signIn("test-token", "test", "test", 10000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectNext("test-token")
                .verifyComplete();

        tokenManager.userIsLoggedIn("test")
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        tokenManager.tokenIsLoggedIn("test-token")
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        tokenManager.getByToken("test-token")
                .map(UserToken::getState)
                .as(StepVerifier::create)
                .expectNext(TokenState.normal)
                .verifyComplete();

        tokenManager.signOutByToken("test-token")
                .as(StepVerifier::create)
                .verifyComplete();

    }


    @Test
    @SneakyThrows
    public void testOfflineOther() {
        tokenManager.signIn("test-token_offline1", "offline", "user1", 1000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectNext("test-token_offline1")
                .verifyComplete();

        tokenManager.signIn("test-token_offline2", "offline", "user1", 1000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectNext("test-token_offline2")
                .verifyComplete();

        tokenManager.getByToken("test-token_offline1")
                .map(UserToken::getState)
                .as(StepVerifier::create)
                .expectNext(TokenState.offline)
                .verifyComplete();
    }

    @Test
    @SneakyThrows
    public void testDeny() {
        tokenManager.signIn("test-token_offline3", "deny", "user2", 1000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectNext("test-token_offline3")
                .verifyComplete();

        tokenManager.signIn("test-token_offline4", "deny", "user2", 1000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectError(AccessDenyException.class)
                .verify();
    }

    @Test
    @SneakyThrows
    public void testSignTimeout() {
        tokenManager.signIn("test-token_2", "test", "test2", 1000)
                .map(UserToken::getToken)
                .as(StepVerifier::create)
                .expectNext("test-token_2")
                .verifyComplete();

        tokenManager.touch("test-token_2")
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        Thread.sleep(2000);
        tokenManager.getByToken("test-token_2")
                .switchIfEmpty(Mono.error(new UnAuthorizedException()))
                .as(StepVerifier::create)
                .expectError(UnAuthorizedException.class)
                .verify();

        tokenManager.getByUserId("test2")
                .count()
                .as(StepVerifier::create)
                .expectNext(0L)
                .verifyComplete();
    }
}