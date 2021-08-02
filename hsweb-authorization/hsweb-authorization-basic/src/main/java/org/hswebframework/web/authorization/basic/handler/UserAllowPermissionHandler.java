package org.hswebframework.web.authorization.basic.handler;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.HandleType;
import org.hswebframework.web.authorization.events.AuthorizingHandleBeforeEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 *     hsweb:
 *        authorize:
 *            allows:
 *               user:
 *                  admin: *
 *                  guest: **.query*
 *               role:
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

    private final PathMatcher pathMatcher = new AntPathMatcher(".");

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

        AtomicBoolean allow = new AtomicBoolean();
        for (Map.Entry<String, Map<String, String>> entry : allows.entrySet()) {
            String dimension = entry.getKey();
            if ("user".equals(dimension)) {
                String userId = context.getAuthentication().getUser().getId();
                allow.set(Optional.ofNullable(entry.getValue().get(userId))
                        .filter(pattern -> "*".equals(pattern) || pathMatcher.match(pattern, path))
                        .isPresent());
            } else { //其他维度
                for (Map.Entry<String, String> confEntry : entry.getValue().entrySet()) {
                    context.getAuthentication()
                            .getDimension(dimension, confEntry.getKey())
                            .ifPresent(dim -> {
                                String pattern = confEntry.getValue();
                                allow.set("*".equals(pattern) || pathMatcher.match(confEntry.getValue(), path));
                            });
                }
            }
            if (allow.get()) {
                event.setAllow(true);
                return;
            }
        }

    }

}
