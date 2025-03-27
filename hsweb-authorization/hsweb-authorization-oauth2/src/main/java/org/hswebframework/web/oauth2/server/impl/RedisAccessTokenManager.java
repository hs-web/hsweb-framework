package org.hswebframework.web.oauth2.server.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.AuthenticationUserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.token.redis.RedisUserTokenManager;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

public class RedisAccessTokenManager implements AccessTokenManager {

    private final ReactiveRedisOperations<String, RedisAccessToken> tokenRedis;

    private final UserTokenManager userTokenManager;

    @Getter
    @Setter
    private int tokenExpireIn = 7200;//2小时

    @Getter
    @Setter
    private int refreshExpireIn = 2592000; //30天

    public RedisAccessTokenManager(ReactiveRedisOperations<String, RedisAccessToken> tokenRedis,
                                   UserTokenManager userTokenManager) {
        this.tokenRedis = tokenRedis;
        this.userTokenManager = userTokenManager;
    }

    @SuppressWarnings("all")
    public RedisAccessTokenManager(ReactiveRedisConnectionFactory connectionFactory) {
        ReactiveRedisTemplate redis = new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext
                .newSerializationContext()
                .key((RedisSerializer) RedisSerializer.string())
                .value(RedisSerializer.java())
                .hashKey(RedisSerializer.string())
                .hashValue(RedisSerializer.java())
                .build());
        this.tokenRedis = redis;
        this.userTokenManager = new RedisUserTokenManager(redis);
    }


    @Override
    public Mono<Authentication> getAuthenticationByToken(String accessToken) {
        return userTokenManager
                .getByToken(accessToken)
                .filter(token -> token instanceof AuthenticationUserToken)
                .map(t -> ((AuthenticationUserToken) t).getAuthentication());
    }

    private String createTokenRedisKey(String clientId, String token) {
        return "oauth2-token:" + clientId + ":" + token;
    }

    private String createUserTokenRedisKey(RedisAccessToken token) {
        return createUserTokenRedisKey(token.getClientId(), token.getAuthentication().getUser().getId());
    }

    private String createUserTokenRedisKey(String clientId, String userId) {
        return "oauth2-user-tokens:" + clientId + ":" + userId;
    }


    private String createRefreshTokenRedisKey(String clientId, String token) {
        return "oauth2-refresh-token:" + clientId + ":" + token;
    }

    private String createSingletonTokenRedisKey(String clientId) {
        return "oauth2-" + clientId + "-token";
    }

    private String createUserTokenRedisKey(String key) {
        return "user-token:".concat(key);
    }

    private Mono<RedisAccessToken> doCreateAccessToken(String clientId, Authentication authentication, boolean singleton) {
        String token = DigestUtils.md5Hex(UUID.randomUUID().toString());
        String refresh = DigestUtils.md5Hex(UUID.randomUUID().toString());
        RedisAccessToken accessToken = new RedisAccessToken(clientId, token, refresh, System.currentTimeMillis(), authentication, singleton);

        return storeToken(accessToken).thenReturn(accessToken);
    }

    private Mono<Void> storeAuthToken(RedisAccessToken token) {
        //保存独立的权限信息,通常是用户指定了特定的授权范围时生效.
        if (token.storeAuth()) {
            return userTokenManager
                    .signIn(token.getAccessToken(),
                            createTokenType(token.getClientId()),
                            token.getAuthentication().getUser().getId(),
                            tokenExpireIn * 1000L,
                            token.getAuthentication())
                    .then();

        } else {
            return userTokenManager
                    .signIn(token.getAccessToken(),
                            createTokenType(token.getClientId()),
                            token.getAuthentication().getUser().getId(),
                            tokenExpireIn * 1000L)
                    .then();
        }
    }

    private Mono<Void> storeToken(RedisAccessToken token) {

        return Flux
                .merge(storeAuthToken(token),
                       tokenRedis
                               .opsForValue()
                               .set(createUserTokenRedisKey(token), token, Duration.ofSeconds(tokenExpireIn)),
                       tokenRedis
                               .opsForValue()
                               .set(createTokenRedisKey(token.getClientId(),
                                                        token.getAccessToken()), token, Duration.ofSeconds(tokenExpireIn)),
                       tokenRedis
                               .opsForValue()
                               .set(createRefreshTokenRedisKey(token.getClientId(),
                                                               token.getRefreshToken()), token, Duration.ofSeconds(refreshExpireIn)))
                .then();
    }

    private Mono<AccessToken> doCreateSingletonAccessToken(String clientId, Authentication authentication) {
        String redisKey = createSingletonTokenRedisKey(clientId);
        return tokenRedis
                .opsForValue()
                .get(redisKey)
                .flatMap(token -> userTokenManager
                        .userIsLoggedIn(authentication.getUser().getId())
                        .filter(flag -> flag)
                        .flatMap(ignore -> tokenRedis
                                .getExpire(redisKey)
                                .map(duration -> token.toAccessToken((int) (duration.toMillis() / 1000))))
                )
                .switchIfEmpty(Mono.defer(() -> doCreateAccessToken(clientId, authentication, true)
                        .flatMap(redisAccessToken -> tokenRedis
                                .opsForValue()
                                .set(redisKey, redisAccessToken, Duration.ofSeconds(tokenExpireIn))
                                .thenReturn(redisAccessToken.toAccessToken(tokenExpireIn))))
                );
    }

    @Override
    public Mono<AccessToken> createAccessToken(String clientId,
                                               Authentication authentication,
                                               boolean singleton) {
        return singleton
                ? doCreateSingletonAccessToken(clientId, authentication)
                : doCreateAccessToken(clientId, authentication, false).map(token -> token.toAccessToken(tokenExpireIn));
    }

    @Override
    public Mono<AccessToken> refreshAccessToken(String clientId, String refreshToken) {
        String redisKey = createRefreshTokenRedisKey(clientId, refreshToken);

        return tokenRedis
                .opsForValue()
                .get(redisKey)
                .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.EXPIRED_REFRESH_TOKEN)))
                .flatMap(token -> {
                    if (!token.getClientId().equals(clientId)) {
                        return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID));
                    }
                    //生成新token
                    String accessToken = DigestUtils.md5Hex(UUID.randomUUID().toString());
                    token.setAccessToken(accessToken);
                    token.setCreateTime(System.currentTimeMillis());
                    return storeToken(token)
                            .as(result -> {
                                // 单例token
                                if (token.isSingleton()) {
                                    return userTokenManager
                                            .signOutByToken(token.getAccessToken())
                                            .then(
                                                    tokenRedis
                                                            .opsForValue()
                                                            .set(createSingletonTokenRedisKey(clientId), token, Duration.ofSeconds(tokenExpireIn))
                                                            .then(result)
                                            )
                                            ;
                                }
                                return result;
                            })
                            .thenReturn(token.toAccessToken(tokenExpireIn));
                });

    }

    @Override
    public Mono<Void> removeToken(String clientId, String token) {

        return Flux
                .merge(userTokenManager.signOutByToken(token),
                       tokenRedis.delete(createSingletonTokenRedisKey(clientId)),
                       tokenRedis.delete(createTokenRedisKey(clientId, token)))
                .then();
    }

    @Override
    public Mono<Void> cancelGrant(String clientId, String userId) {
        //删除最新的refresh_token
        Mono<Void> removeRefreshToken = tokenRedis
                .opsForValue()
                .get(createUserTokenRedisKey(clientId, userId))
                .flatMap(t -> tokenRedis
                        .opsForValue()
                        .delete(createRefreshTokenRedisKey(t.getClientId(), t.getRefreshToken())))
                .then();

        //删除access_token
        Mono<Void> removeAccessToken = userTokenManager
                .getByUserId(userId)
                .flatMap(token -> {
                    //其他类型的token 忽略
                    if (!(createTokenType(clientId)).equals(token.getType())) {
                        return Mono.empty();
                    }
                    return tokenRedis
                            .opsForValue()
                            .get(createTokenRedisKey(clientId, token.getToken()))
                            .flatMap(t -> {
                                //移除token
                                return tokenRedis
                                        .delete(createTokenRedisKey(t.getClientId(), t.getAccessToken()))
                                        //移除token对应的refresh_token
                                        .then(tokenRedis
                                                      .opsForValue()
                                                      .delete(createRefreshTokenRedisKey(t.getClientId(), t.getRefreshToken())));
                            })
                            .then(userTokenManager.signOutByToken(token.getToken()));
                })
                .then();

        return Flux
                .merge(removeRefreshToken, removeAccessToken)
                .then();
    }

    private String createTokenType(String clientId) {
        return "oauth2-" + clientId;
    }
}
