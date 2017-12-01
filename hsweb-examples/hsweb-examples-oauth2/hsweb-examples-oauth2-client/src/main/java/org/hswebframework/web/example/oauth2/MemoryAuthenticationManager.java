package org.hswebframework.web.example.oauth2;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthenticationManager implements AuthenticationManager {
    static Map<String, Authentication> users = new HashMap<>();

    public static void addAuthentication(Authentication authentication) {
        users.put(authentication.getUser().getId(), authentication);
    }

    @Override
    public Authentication getByUserId(String userId) {

        return users.get(userId);
    }

    @Override
    public Authentication sync(Authentication authentication) {
        return authentication;
    }
}
