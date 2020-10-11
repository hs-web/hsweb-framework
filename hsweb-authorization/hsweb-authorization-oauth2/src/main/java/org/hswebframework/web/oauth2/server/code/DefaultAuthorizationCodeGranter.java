package org.hswebframework.web.oauth2.server.code;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.function.BiPredicate;

@AllArgsConstructor
public class DefaultAuthorizationCodeGranter implements AuthorizationCodeGranter {

    private final AccessTokenManager accessTokenManager;

    private final ReactiveRedisOperations<String, AuthorizationCodeCache> redis;

    @SuppressWarnings("all")
    public DefaultAuthorizationCodeGranter(AccessTokenManager accessTokenManager, ReactiveRedisConnectionFactory connectionFactory) {
        this(accessTokenManager, new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext
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
        request.getParameter("scope").map(String::valueOf).ifPresent(codeCache::setScope);
        codeCache.setCode(code);
        codeCache.setClientId(client.getClientId());
        codeCache.setAuthentication(authentication.copy(createPredicate(codeCache.getScope()), dimension -> true));

        createPredicate(codeCache.getScope());

        return redis
                .opsForValue()
                .set(getRedisKey(code), codeCache, Duration.ofMinutes(5))
                .thenReturn(new AuthorizationCodeResponse(code));
    }

    static BiPredicate<Permission, String> createPredicate(String scopeStr) {
        if (StringUtils.isEmpty(scopeStr)) {
            return ((permission, s) -> false);
        }
        String[] scopes = scopeStr.split("[ ,\n]");
        Map<String, Set<String>> actions = new HashMap<>();
        for (String scope : scopes) {
            String[] permissions = scope.split("[:]");
            String per = permissions[0];
            Set<String> acts = actions.computeIfAbsent(per, k -> new HashSet<>());
            acts.addAll(Arrays.asList(permissions).subList(1, permissions.length));
        }

        return ((permission, action) -> Optional
                .ofNullable(actions.get(permission.getId()))
                .map(acts -> acts.contains(action))
                .orElse(false));
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
                .flatMap(cache -> redis
                        .opsForValue()
                        .delete(getRedisKey(cache.getCode()))
                        .thenReturn(cache))
                .flatMap(cache -> {
                    if (!request.getClient().getClientId().equals(cache.getClientId())) {
                        return Mono.error(new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_ID));
                    }
                    return accessTokenManager.createAccessToken(cache.getClientId(), cache.getAuthentication(), false);
                });

    }
}
