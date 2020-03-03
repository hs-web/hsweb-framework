package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.context.ContextKey;
import org.hswebframework.web.context.ContextUtils;
import org.hswebframework.web.logger.ReactiveLogger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 */
public class UserTokenReactiveAuthenticationSupplier implements ReactiveAuthenticationSupplier {

    private ReactiveAuthenticationManager defaultAuthenticationManager;

    private UserTokenManager userTokenManager;

    private Map<String, ThirdPartReactiveAuthenticationManager> thirdPartAuthenticationManager = new HashMap<>();

    public UserTokenReactiveAuthenticationSupplier(UserTokenManager userTokenManager, ReactiveAuthenticationManager defaultAuthenticationManager) {
        this.defaultAuthenticationManager = defaultAuthenticationManager;
        this.userTokenManager = userTokenManager;
    }

    @Autowired(required = false)
    public void setThirdPartAuthenticationManager(List<ThirdPartReactiveAuthenticationManager> thirdPartReactiveAuthenticationManager) {
        for (ThirdPartReactiveAuthenticationManager manager : thirdPartReactiveAuthenticationManager) {
            this.thirdPartAuthenticationManager.put(manager.getTokenType(), manager);
        }
    }

    @Override
    public Mono<Authentication> get(String userId) {
        if (userId == null) {
            return null;
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
        return ContextUtils.reactiveContext()
                .flatMap(context ->
                        context.get(ContextKey.of(ParsedToken.class))
                                .map(t -> userTokenManager
                                        .getByToken(t.getToken())
                                        .filter(UserToken::validate))
                                .map(tokenMono -> tokenMono
                                        .flatMap(token -> userTokenManager.touch(token.getToken()).thenReturn(token))
                                        .flatMap(token -> get(thirdPartAuthenticationManager.get(token.getType()), token.getUserId())))
                                .orElseGet(Mono::empty))
                .flatMap(auth -> ReactiveLogger.mdc("userId", auth.getUser().getId())
                        .then(ReactiveLogger.mdc("username", auth.getUser().getName()))
                        .thenReturn(auth))
                ;

    }
}
