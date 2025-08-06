package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.*;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 */
public class UserTokenAuthenticationSupplier implements AuthenticationSupplier {

    private AuthenticationManager defaultAuthenticationManager;

    private UserTokenManager userTokenManager;

    private Map<String, ThirdPartAuthenticationManager> thirdPartAuthenticationManager = new HashMap<>();

    public UserTokenAuthenticationSupplier(UserTokenManager userTokenManager, AuthenticationManager defaultAuthenticationManager) {
        this.defaultAuthenticationManager = defaultAuthenticationManager;
        this.userTokenManager = userTokenManager;
    }

    @Autowired(required = false)
    public void setThirdPartAuthenticationManager(List<ThirdPartAuthenticationManager> thirdPartReactiveAuthenticationManager) {
        for (ThirdPartAuthenticationManager manager : thirdPartReactiveAuthenticationManager) {
            this.thirdPartAuthenticationManager.put(manager.getTokenType(), manager);
        }
    }

    @Override
    public Optional<Authentication> get(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return get(this.defaultAuthenticationManager, userId);
    }

    protected Optional<Authentication> get(ThirdPartAuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return Optional.empty();
        }
        if (null == authenticationManager) {
            return this.defaultAuthenticationManager.getByUserId(userId);
        }
        return authenticationManager.getByUserId(userId);
    }

    protected Optional<Authentication> get(AuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return Optional.empty();
        }
        if (null == authenticationManager) {
            authenticationManager = this.defaultAuthenticationManager;
        }
        return authenticationManager.getByUserId(userId);
    }

    @Override
    public Optional<Authentication> get() {


        return Optional
            .ofNullable(UserTokenHolder.currentToken())
            .map(t -> userTokenManager.getByToken(t.getToken()))
            .map(tokenMono -> tokenMono
                .map(token -> get(thirdPartAuthenticationManager.get(token.getType()), token.getUserId()))
                .flatMap(Mono::justOrEmpty))
            .flatMap(Mono::blockOptional);

    }
}
