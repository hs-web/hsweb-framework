package org.hswebframework.web.oauth2.server.code;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.GrantType;
import org.hswebframework.web.oauth2.OAuth2Constants;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.ScopePredicate;
import org.hswebframework.web.oauth2.server.event.OAuth2GrantedEvent;
import org.hswebframework.web.oauth2.server.utils.OAuth2ScopeUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Mono;

import java.time.Duration;

@AllArgsConstructor
public class DefaultAuthorizationCodeGranter implements AuthorizationCodeGranter {

    private final AccessTokenManager accessTokenManager;

    private final ApplicationEventPublisher eventPublisher;

    private final ReactiveRedisOperations<String, AuthorizationCodeCache> redis;

    @SuppressWarnings("all")
    public DefaultAuthorizationCodeGranter(AccessTokenManager accessTokenManager,
                                           ApplicationEventPublisher eventPublisher,
                                           ReactiveRedisConnectionFactory connectionFactory) {
        this(accessTokenManager, eventPublisher, new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext
                .newSerializationContext()
                .key((RedisSerializer) RedisSerializer.string())
                .value(RedisSerializer.java())
                .hashKey(RedisSerializer.string())
                .hashValue(RedisSerializer.java())
                .build()
        ));
    }

    @Override
    public Mono<AuthorizationCodeResponse> requestCode(AuthorizationCodeRequest request) {
        OAuth2Client client = request.getClient();
        Authentication authentication = request.getAuthentication();
        AuthorizationCodeCache codeCache = new AuthorizationCodeCache();
        String code = IDGenerator.MD5.generate();
        request.getParameter(OAuth2Constants.scope).map(String::valueOf).ifPresent(codeCache::setScope);
        codeCache.setCode(code);
        codeCache.setClientId(client.getClientId());

        ScopePredicate permissionPredicate = OAuth2ScopeUtils.createScopePredicate(codeCache.getScope());

        codeCache.setAuthentication(authentication.copy(
                (permission, action) -> permissionPredicate.test(permission.getId(), action),
                dimension -> permissionPredicate.test(dimension.getType().getId(), dimension.getId())));


        return redis
                .opsForValue()
                .set(getRedisKey(code), codeCache, Duration.ofMinutes(5))
                .thenReturn(new AuthorizationCodeResponse(code));
    }


    private String getRedisKey(String code) {
        return "oauth2-code:" + code;
    }

    @Override
    public Mono<AccessToken> requestToken(AuthorizationCodeTokenRequest request) {

        return Mono
                .justOrEmpty(request.code())
                .map(this::getRedisKey)
                .flatMap(redis.opsForValue()::get)
                .switchIfEmpty(Mono.error(() -> new OAuth2Exception(ErrorType.ILLEGAL_CODE)))
                //移除code
                .flatMap(cache -> redis.opsForValue().delete(getRedisKey(cache.getCode())).thenReturn(cache))
                .flatMap(cache -> {
                    if (!request.getClient().getClientId().equals(cache.getClientId())) {
                        return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID));
                    }
                    return accessTokenManager
                            .createAccessToken(cache.getClientId(), cache.getAuthentication(), false)
                            .flatMap(token -> new OAuth2GrantedEvent(request.getClient(),
                                                                     token,
                                                                     cache.getAuthentication(),
                                                                     cache.getScope(),
                                                                     GrantType.authorization_code,
                                                                     request.getParameters())
                                    .publish(eventPublisher)
                                    .onErrorResume(err -> accessTokenManager
                                            .removeToken(cache.getClientId(), token.getAccessToken())
                                            .then(Mono.error(err)))
                                    .thenReturn(token));
                })
                ;

    }
}
