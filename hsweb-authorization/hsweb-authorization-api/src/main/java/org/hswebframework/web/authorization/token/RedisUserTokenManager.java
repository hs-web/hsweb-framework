package org.hswebframework.web.authorization.token;

import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisUserTokenManager implements UserTokenManager {

    ReactiveRedisOperations<Object, Object> operations;

    ReactiveHashOperations<String, String, UserToken> userTokenStore;

    ReactiveSetOperations<String, String> userTokenMapping;

    Map<String,UserToken> localCache=new ConcurrentHashMap<>();

    @Override
    public Mono<UserToken> getByToken(String token) {

        return null;
    }

    @Override
    public Flux<UserToken> getByUserId(String userId) {
        return null;
    }

    @Override
    public Mono<Boolean> userIsLoggedIn(String userId) {
        return null;
    }

    @Override
    public Mono<Boolean> tokenIsLoggedIn(String token) {
        return null;
    }

    @Override
    public Mono<Integer> totalUser() {
        return null;
    }

    @Override
    public Mono<Integer> totalToken() {
        return null;
    }

    @Override
    public Flux<UserToken> allLoggedUser() {
        return null;
    }

    @Override
    public Mono<Void> signOutByUserId(String userId) {
        return null;
    }

    @Override
    public Mono<Void> signOutByToken(String token) {
        return null;
    }

    @Override
    public Mono<Void> changeUserState(String userId, TokenState state) {
        return null;
    }

    @Override
    public Mono<Void> changeTokenState(String token, TokenState state) {
        return null;
    }

    @Override
    public Mono<UserToken> signIn(String token, String type, String userId, long maxInactiveInterval) {
        return null;
    }

    @Override
    public Mono<Void> touch(String token) {
        return null;
    }

    @Override
    public Mono<Void> checkExpiredToken() {
        return null;
    }
}
