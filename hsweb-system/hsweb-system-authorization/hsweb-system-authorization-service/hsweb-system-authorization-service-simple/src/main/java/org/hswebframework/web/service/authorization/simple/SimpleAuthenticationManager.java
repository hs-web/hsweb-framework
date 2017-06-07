package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationInitializeService;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_AUTH_CACHE_NAME;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthenticationManager implements AuthenticationManager {

    private AuthenticationInitializeService authenticationInitializeService;

    public SimpleAuthenticationManager() {
    }

    public SimpleAuthenticationManager(AuthenticationInitializeService authenticationInitializeService) {
        this.authenticationInitializeService = authenticationInitializeService;
    }

    @Autowired
    public void setAuthenticationInitializeService(AuthenticationInitializeService authenticationInitializeService) {
        this.authenticationInitializeService = authenticationInitializeService;
    }

    @Override
    @Cacheable(value = USER_AUTH_CACHE_NAME, key = "#userId")
    public Authentication getByUserId(String userId) {
        return authenticationInitializeService.initUserAuthorization(userId);
    }

    @Override
    @CachePut(value = USER_AUTH_CACHE_NAME, key = "#authentication.user.id")
    public Authentication sync(Authentication authentication) {
        return authentication;
    }
}
