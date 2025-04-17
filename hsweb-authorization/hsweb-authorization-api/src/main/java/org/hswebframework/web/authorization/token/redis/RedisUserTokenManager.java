package org.hswebframework.web.authorization.token.redis;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.token.*;
import org.hswebframework.web.authorization.token.event.UserTokenChangedEvent;
import org.hswebframework.web.authorization.token.event.UserTokenCreatedEvent;
import org.hswebframework.web.authorization.token.event.UserTokenRemovedEvent;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.event.AsyncEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RedisUserTokenManager implements UserTokenManager {

    private final ReactiveRedisOperations<Object, Object> operations;

    private final ReactiveHashOperations<Object, String, Object> userTokenStore;

    private final ReactiveSetOperations<Object, Object> userTokenMapping;

    @Setter
    private Map<String, SimpleUserToken> localCache = new ConcurrentHashMap<>();

    private FluxSink<UserToken> touchSink;

    public RedisUserTokenManager(ReactiveRedisOperations<Object, Object> operations) {
        this.operations = operations;
        this.userTokenStore = operations.opsForHash();
        this.userTokenMapping = operations.opsForSet();
        this.operations
            .listenToChannel("_user_token_removed")
            .subscribe(msg -> localCache.remove(String.valueOf(msg.getMessage())));

        Flux.<UserToken>create(sink -> this.touchSink = sink)
            .buffer(Flux.interval(Duration.ofSeconds(10)), HashSet::new)
            .flatMap(list -> Flux
                .fromIterable(list)
                .flatMap(token -> {
                    String key = getTokenRedisKey(token.getToken());
                    return Mono
                        .zip(this.userTokenStore.put(key, "lastRequestTime", token.getLastRequestTime()),
                             this.operations.expire(key, Duration.ofMillis(token.getMaxInactiveInterval())))
                        .then();
                })
                .onErrorResume(err -> Mono.empty()))
            .subscribe();

    }

    @SuppressWarnings("all")
    public RedisUserTokenManager(ReactiveRedisConnectionFactory connectionFactory) {
        this(new ReactiveRedisTemplate<>(connectionFactory,
                                         RedisSerializationContext
                                             .newSerializationContext()
                                             .key((RedisSerializer) RedisSerializer.string())
                                             .value(RedisSerializer.java())
                                             .hashKey(RedisSerializer.string())
                                             .hashValue(RedisSerializer.java())
                                             .build()
        ));
    }

    @Getter
    @Setter
    private Map<String, AllopatricLoginMode> allopatricLoginModes = new HashMap<>();

    @Getter
    @Setter
    //异地登录模式，默认允许异地登录
    private AllopatricLoginMode allopatricLoginMode = AllopatricLoginMode.allow;

    @Getter
    @Setter
    private Duration maxTokenExpires = Duration.ofSeconds(1).negated();

    @Setter
    private ApplicationEventPublisher eventPublisher;

    private String getTokenRedisKey(String key) {
        return "user-token:".concat(key);
    }

    private String getUserRedisKey(String key) {
        return "user-token-user:".concat(key);
    }

    @Override
    public Mono<UserToken> getByToken(String token) {
        SimpleUserToken inCache = localCache.get(token);
        if (inCache != null && inCache.isNormal()) {
            return Mono.just(inCache);
        }
        return userTokenStore
            .entries(getTokenRedisKey(token))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            .filter(map -> !map.isEmpty() && map.containsKey("token") && map.containsKey("userId"))
            .map(SimpleUserToken::of)
            .doOnNext(userToken -> localCache.put(userToken.getToken(), userToken))
            .cast(UserToken.class);
    }

    @Override
    public Flux<UserToken> getByUserId(String userId) {
        String redisKey = getUserRedisKey(userId);
        return userTokenMapping
            .members(redisKey)
            .map(String::valueOf)
            .flatMap(token -> getByToken(token)
                .switchIfEmpty(Mono.defer(() -> userTokenMapping
                    .remove(redisKey, token)
                    .then(Mono.empty()))));
    }

    @Override
    public Mono<Boolean> userIsLoggedIn(String userId) {
        return getByUserId(userId)
            .any(UserToken::isNormal);
    }

    @Override
    public Mono<Boolean> tokenIsLoggedIn(String token) {
        return getByToken(token)
            .map(UserToken::isNormal)
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<Integer> totalUser() {

        return operations
            .scan(ScanOptions
                      .scanOptions()
                      .match("*user-token-user:*")
                      .build())
            .count()
            .map(Long::intValue);
    }

    @Override
    public Mono<Integer> totalToken() {
        return operations
            .scan(ScanOptions
                      .scanOptions()
                      .match("*user-token:*")
                      .build())
            .count()
            .map(Long::intValue);
    }

    @Override
    public Flux<UserToken> allLoggedUser() {
        return operations
            .scan(ScanOptions
                      .scanOptions()
                      .match("*user-token:*")
                      .build())
            .map(val -> String.valueOf(val).substring(11))
            .flatMap(this::getByToken);
    }

    @Override
    public Mono<Void> signOutByUserId(String userId) {
        return this
            .getByUserId(userId)
            .flatMap(userToken -> operations
                .delete(getTokenRedisKey(userToken.getToken()))
                .then(onTokenRemoved(userToken)))
            .then(operations.delete(getUserRedisKey(userId)))
            .then();
    }

    @Override
    public Mono<Void> signOutByToken(String token) {
        //delete token
        //srem user token
        return getByToken(token)
            .flatMap(t -> operations
                .delete(getTokenRedisKey(t.getToken()))
                .then(userTokenMapping.remove(getUserRedisKey(t.getUserId()), token))
                .then(onTokenRemoved(t))
            )
            .then();
    }

    @Override
    public Mono<Void> changeUserState(String userId, TokenState state) {

        return getByUserId(userId)
            .flatMap(token -> changeTokenState(token.getToken(), state))
            .then();
    }

    @Override
    public Mono<Void> changeTokenState(String token, TokenState state) {

        return getByToken(token)
            .flatMap(old -> {
                SimpleUserToken newToken = FastBeanCopier.copy(old, new SimpleUserToken());
                newToken.setState(state);
                return userTokenStore
                    .put(getTokenRedisKey(token), "state", state.getValue())
                    .then(onTokenChanged(old, newToken));
            });
    }

    protected Mono<SimpleUserToken> sign0(String token,
                                          String type,
                                          String userId,
                                          long expires,
                                          boolean ignoreAllopatricLoginMode,
                                          Consumer<Map<String, Object>> cacheBuilder) {
        return Mono.defer(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("type", type);
            map.put("userId", userId);
            map.put("maxInactiveInterval", expires);
            map.put("state", TokenState.normal.getValue());
            map.put("signInTime", System.currentTimeMillis());
            map.put("lastRequestTime", System.currentTimeMillis());
            cacheBuilder.accept(map);
            String key = getTokenRedisKey(token);
            SimpleUserToken userToken = SimpleUserToken.of(map);

            // 推送事件,自定义过期时间等场景
            UserTokenBeforeCreateEvent event = new UserTokenBeforeCreateEvent(userToken, expires);

            return this
                .publishEvent(event)
                .then(Mono.defer(() -> {
                    map.put("maxInactiveInterval", event.getExpires());
                    return userTokenStore.putAll(key, map);
                }))
                .then(Mono.defer(() -> {
                    if (expires > 0) {
                        return operations.expire(key, Duration.ofMillis(event.getExpires()));
                    }
                    return Mono.empty();
                }))
                .then(userTokenMapping.add(getUserRedisKey(userId), token))
                .thenReturn(SimpleUserToken.of(map));
        });
    }

    private Mono<UserToken> signIn(String token,
                                   String type,
                                   String userId,
                                   long maxInactiveInterval,
                                   boolean ignoreAllopatricLoginMode,
                                   Consumer<Map<String, Object>> cacheBuilder) {
        long expires = maxTokenExpires.isNegative() ? maxInactiveInterval : Math.min(maxInactiveInterval, maxTokenExpires.toMillis());

        return Mono
            .defer(() -> {
                Mono<SimpleUserToken> doSign = sign0(
                    token,
                    type,
                    userId,
                    expires,
                    ignoreAllopatricLoginMode,
                    cacheBuilder
                );

                if (ignoreAllopatricLoginMode) {
                    return doSign;
                }
                AllopatricLoginMode mode = allopatricLoginModes.getOrDefault(type, allopatricLoginMode);
                if (mode == AllopatricLoginMode.deny) {
                    return userIsLoggedIn(userId)
                        .flatMap(r -> {
                            if (r) {
                                return Mono.error(new AccessDenyException("error.logged_in_elsewhere", TokenState.deny.getValue()));
                            }
                            return doSign;
                        });

                } else if (mode == AllopatricLoginMode.offlineOther) {
                    return getByUserId(userId)
                        .flatMap(userToken -> {
                            if (type.equals(userToken.getType())) {
                                return this.changeTokenState(userToken.getToken(), TokenState.offline);
                            }
                            return Mono.empty();
                        })
                        .then(doSign);
                }

                return doSign;
            })
            .flatMap(this::onUserTokenCreated);
    }

    @Override
    public Mono<UserToken> signIn(String token, String type, String userId, long maxInactiveInterval) {
        return signIn(token, type, userId, maxInactiveInterval, false, ignore -> {
        });
    }

    @Override
    public Mono<AuthenticationUserToken> signIn(String token,
                                                String type,
                                                String userId,
                                                long maxInactiveInterval,
                                                Authentication authentication) {
        return this
            .signIn(token, type, userId, maxInactiveInterval,
                    true,
                    cache -> cache.put("authentication", authentication))
            .cast(AuthenticationUserToken.class);
    }

    @Override
    public Mono<Void> touch(String token) {
        SimpleUserToken inCache = localCache.get(token);
        if (inCache != null && inCache.isNormal()) {
            inCache.setLastRequestTime(System.currentTimeMillis());
            if (inCache.getMaxInactiveInterval() > 0) {
                //异步touch
                touchSink.next(inCache);
            }
            return Mono.empty();
        }
        return getByToken(token)
            .flatMap(userToken -> {
                if (userToken.getMaxInactiveInterval() > 0) {
                    touchSink.next(userToken);
                }
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> checkExpiredToken() {

        return operations
            .scan(ScanOptions.scanOptions().match("*user-token-user:*").build())
            .map(String::valueOf)
            .flatMap(key -> userTokenMapping
                .members(key)
                .map(String::valueOf)
                .flatMap(token -> operations
                    .hasKey(getTokenRedisKey(token))
                    .flatMap(exists -> {
                        if (!exists) {
                            return userTokenMapping.remove(key, token);
                        }
                        return Mono.empty();
                    })))
            .then();
    }

    private Mono<Void> notifyTokenRemoved(String token) {
        return operations.convertAndSend("_user_token_removed", token).then();
    }

    private Mono<Void> onTokenRemoved(UserToken token) {
        localCache.remove(token.getToken());

        if (eventPublisher == null) {
            return notifyTokenRemoved(token.getToken());
        }
        return new UserTokenRemovedEvent(token)
            .publish(eventPublisher)
            .then(notifyTokenRemoved(token.getToken()));
    }

    private Mono<Void> onTokenChanged(UserToken old, SimpleUserToken newToken) {
        localCache.put(newToken.getToken(), newToken);
        if (eventPublisher == null) {
            return notifyTokenRemoved(newToken.getToken());
        }
        return new UserTokenChangedEvent(old, newToken)
            .publish(eventPublisher)
            .then(notifyTokenRemoved(newToken.getToken()));
    }

    private Mono<Void> publishEvent(AsyncEvent event) {
        if (eventPublisher != null) {
            return event.publish(eventPublisher);
        }
        return Mono.empty();
    }

    private Mono<UserToken> onUserTokenCreated(SimpleUserToken token) {
        localCache.put(token.getToken(), token);
        if (eventPublisher == null) {
            return notifyTokenRemoved(token.getToken())
                .thenReturn(token);
        }
        return new UserTokenCreatedEvent(token)
            .publish(eventPublisher)
            .then(notifyTokenRemoved(token.getToken()))
            .thenReturn(token);
    }

}
