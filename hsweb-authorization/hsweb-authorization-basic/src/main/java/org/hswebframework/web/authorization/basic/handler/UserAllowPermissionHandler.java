package org.hswebframework.web.authorization.basic.handler;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.HandleType;
import org.hswebframework.web.authorization.listener.event.AuthorizingHandleBeforeEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * <pre>
 *     hsweb:
 *        authorize:
 *            allows:
 *               users:
 *                  admin: *
 *                  guest: **.query*
 *               roles:
 *                  admin: *
 *
 * </pre>
 *
 * @author zhouhao
 * @since 3.0.1
 */
@ConfigurationProperties("hsweb.authorize")
public class UserAllowPermissionHandler {

    @Getter
    @Setter
    private Map<String, Map<String, String>> allows = new HashMap<>();

    private PathMatcher pathMatcher = new AntPathMatcher(".");

    @EventListener
    public void handEvent(AuthorizingHandleBeforeEvent event) {

        if (allows.isEmpty() || event.getHandleType() == HandleType.DATA) {
            return;
        }
        AuthorizingContext context = event.getContext();

        // class full name.method
        String path = ClassUtils.getUserClass(context.getParamContext()
                .getTarget())
                .getName().concat(".")
                .concat(context.getParamContext()
                        .getMethod().getName());

        String userId = context.getAuthentication().getUser().getId();
        boolean allow;
        allow = Optional.ofNullable(allows.get("users"))
                .map(users -> users.get(userId))
                .filter(pattern -> "*".equals(pattern) || pathMatcher.match(pattern, path))
                .isPresent();
        if (allow) {
            event.setAllow(true);
            return;
        }
        allow = context.getAuthentication()
                .getRoles()
                .stream()
                .map(role -> allows.getOrDefault("roles", Collections.emptyMap()).get(role.getId()))
                .filter(Objects::nonNull)
                .anyMatch(pattern -> "*".equals(pattern) || pathMatcher.match(pattern, path));
        if (allow) {
            event.setAllow(true);
            return;
        }
    }

}
