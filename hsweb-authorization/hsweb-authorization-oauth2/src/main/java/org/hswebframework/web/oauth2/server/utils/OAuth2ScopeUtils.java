package org.hswebframework.web.oauth2.server.utils;

import org.hswebframework.web.oauth2.server.ScopePredicate;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author zhouhao
 * @since 4.0.8
 */
public class OAuth2ScopeUtils {

    public static ScopePredicate createScopePredicate(String scopeStr) {
        if (StringUtils.isEmpty(scopeStr)) {
            return ((permission, action) -> false);
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
                .ofNullable(actions.get(permission))
                .map(acts -> action.length == 0 || acts.containsAll(Arrays.asList(action)))
                .orElse(false));
    }
}
