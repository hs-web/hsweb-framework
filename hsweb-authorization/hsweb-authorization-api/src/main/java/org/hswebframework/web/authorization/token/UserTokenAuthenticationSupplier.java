package org.hswebframework.web.authorization.token;

import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 */
public class UserTokenAuthenticationSupplier implements AuthenticationSupplier {

    private AuthenticationManager defaultAuthenticationManager;

    private Map<String, ThirdPartAuthenticationManager> thirdPartAuthenticationManager = new HashMap<>();

    public UserTokenAuthenticationSupplier(AuthenticationManager defaultAuthenticationManager) {
        this.defaultAuthenticationManager = defaultAuthenticationManager;
    }

    @Autowired(required = false)
    public void setThirdPartAuthenticationManager(List<ThirdPartAuthenticationManager> thirdPartAuthenticationManager) {
        for (ThirdPartAuthenticationManager manager : thirdPartAuthenticationManager) {
            this.thirdPartAuthenticationManager.put(manager.getTokenType(), manager);
        }
    }

    @Override
    public Authentication get(String userId) {
        if (userId == null) {
            return null;
        }
        return get(this.defaultAuthenticationManager, userId);
    }

    protected Authentication get(ThirdPartAuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return null;
        }
        if (null == authenticationManager) {
            return this.defaultAuthenticationManager.getByUserId(userId);
        }
        return authenticationManager.getByUserId(userId);
    }

    protected Authentication get(AuthenticationManager authenticationManager, String userId) {
        if (null == userId) {
            return null;
        }
        if (null == authenticationManager) {
            authenticationManager = this.defaultAuthenticationManager;
        }
        return authenticationManager.getByUserId(userId);
    }

    protected UserToken getCurrentUserToken() {
        return UserTokenHolder.currentToken();
    }

    @Override
    public Authentication get() {
        return ThreadLocalUtils.get(Authentication.class.getName(), () ->
                Optional.ofNullable(getCurrentUserToken())
                        .filter(UserToken::validate) //验证token,如果不是正常状态,将会抛出异常
                        .map(token ->
                                get(thirdPartAuthenticationManager
                                        .get(token.getType()), token.getUserId())
                        )
                        .orElse(null));
    }
}
