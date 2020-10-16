package org.hswebframework.web.authorization.token.redis;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.TokenAuthenticationManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class RedisTokenAuthenticationManager implements TokenAuthenticationManager {

    private final ReactiveRedisOperations<String, Authentication> operations;

    @SuppressWarnings("all")
    public RedisTokenAuthenticationManager(ReactiveRedisConnectionFactory connectionFactory) {
        this(new ReactiveRedisTemplate<>(
                connectionFactory, RedisSerializationContext.<String, Authentication>newSerializationContext()
                .key(RedisSerializer.string())
                .value((RedisSerializer) RedisSerializer.java())
                .hashKey(RedisSerializer.string())
                .hashValue(RedisSerializer.java())
                .build()
        ));
    }

    public RedisTokenAuthenticationManager(ReactiveRedisOperations<String, Authentication> operations) {
        this.operations = operations;
    }

    @Override
    public Mono<Authentication> getByToken(String token) {
        return operations
                .opsForValue()
                .get("token-auth:" + token);
    }

    @Override
    public Mono<Void> removeToken(String token) {
        return operations
                .delete(token)
                .then();
    }

    @Override
    public Mono<Void> putAuthentication(String token, Authentication auth, Duration ttl) {
        return ttl.isNegative()
                ? operations
                .opsForValue()
                .set("token-auth:" + token, auth)
                .then()
                : operations
                .opsForValue()
                .set("token-auth:" + token, auth, ttl)
                .then()
                ;
    }
}
