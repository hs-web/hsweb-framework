package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据 Reactor Context 中的 {@link ParsedToken} 提供当前响应式权限信息。
 *
 * <p>请求令牌类型由 {@link ReactiveTokenAuthenticationProvider} 唯一拥有时直接执行原始
 * 令牌认证；其他类型继续使用 {@link UserTokenManager} 的有状态令牌校验与第三方权限加载
 * 链路。</p>
 *
 * @author zhouhao
 * @see ReactiveTokenAuthenticationProvider
 * @see ThirdPartReactiveAuthenticationManager
 */
public class UserTokenReactiveAuthenticationSupplier implements ReactiveAuthenticationSupplier {

    private final ReactiveAuthenticationManager defaultAuthenticationManager;

    private final UserTokenManager userTokenManager;

    private final Map<String, ThirdPartReactiveAuthenticationManager> thirdPartAuthenticationManager = new HashMap<>();

    private final Map<String, ReactiveTokenAuthenticationProvider> tokenAuthenticationProviders = new HashMap<>();

    public UserTokenReactiveAuthenticationSupplier(UserTokenManager userTokenManager,
                                                   ReactiveAuthenticationManager defaultAuthenticationManager) {
        this.defaultAuthenticationManager = defaultAuthenticationManager;
        this.userTokenManager = userTokenManager;
    }

    @Autowired(required = false)
    public void setThirdPartAuthenticationManager(List<ThirdPartReactiveAuthenticationManager> thirdPartReactiveAuthenticationManager) {
        for (ThirdPartReactiveAuthenticationManager manager : thirdPartReactiveAuthenticationManager) {
            this.thirdPartAuthenticationManager.put(manager.getTokenType(), manager);
        }
    }

    @Autowired(required = false)
    public void setTokenAuthenticationProviders(List<ReactiveTokenAuthenticationProvider> providers) {
        for (ReactiveTokenAuthenticationProvider provider : providers) {
            String tokenType = provider.getTokenType();
            Assert.hasText(tokenType, "tokenType must not be empty");
            ReactiveTokenAuthenticationProvider previous = tokenAuthenticationProviders.putIfAbsent(tokenType, provider);
            Assert.state(previous == null, () -> "duplicate token authentication provider type: " + tokenType);
        }
    }

    @Override
    public Mono<Authentication> get(String userId) {
        if (userId == null) {
            return Mono.empty();
        }
        return get(this.defaultAuthenticationManager, userId);
    }

    protected Mono<Authentication> get(ThirdPartReactiveAuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return null;
        }
        if (null == authenticationManager) {
            return this.defaultAuthenticationManager.getByUserId(userId);
        }
        return authenticationManager.getByUserId(userId);
    }

    protected Mono<Authentication> get(ReactiveAuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return null;
        }
        if (null == authenticationManager) {
            authenticationManager = this.defaultAuthenticationManager;
        }
        return authenticationManager.getByUserId(userId);
    }

    @Override
    public Mono<Authentication> get() {
        return Mono
            .deferContextual(context -> context
                .<ParsedToken>getOrEmpty(ParsedToken.class)
                .map(this::getByToken)
                .orElse(Mono.empty()))
            ;

    }

    private Mono<Authentication> getByToken(ParsedToken parsedToken) {
        ReactiveTokenAuthenticationProvider provider = tokenAuthenticationProviders.get(parsedToken.getType());
        if (provider != null) {
            // 类型一旦被 Provider 声明就不能回退旧令牌存储，避免非法结构化令牌触发存储查询。
            return provider.authenticate(parsedToken);
        }
        return getByUserToken(parsedToken);
    }

    private Mono<Authentication> getByUserToken(ParsedToken parsedToken) {
        return userTokenManager
            .getByToken(parsedToken.getToken())
            .flatMap(token -> {
                // 已过期则返回空
                if (token.isExpired()) {
                    return Mono.empty();
                }
                if (!token.validate()) {
                    return Mono.empty();
                }
                Mono<Void> before = userTokenManager.touch(token.getToken());
                if (token instanceof AuthenticationUserToken) {
                    return before.thenReturn(((AuthenticationUserToken) token).getAuthentication());
                }
                return before.then(get(thirdPartAuthenticationManager.get(token.getType()), token.getUserId()));
            });
    }
}
