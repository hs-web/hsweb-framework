package org.hswebframework.web.system.authorization.api.event;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhouhao
 * @see org.springframework.context.event.EventListener
 * @since 3.0.0-RC
 */
@Getter
public class ClearUserAuthorizationCacheEvent {
    private Set<String> userId;

    private boolean all;

    public static ClearUserAuthorizationCacheEvent of(Collection<String> collection) {
        ClearUserAuthorizationCacheEvent event = new ClearUserAuthorizationCacheEvent();
        if (collection == null || collection.isEmpty()) {
            event.all = true;
        } else {
            event.userId = new HashSet<>(collection);
        }
        return event;
    }

    public static ClearUserAuthorizationCacheEvent all() {
        return ClearUserAuthorizationCacheEvent.of((String[]) null);
    }

    public static ClearUserAuthorizationCacheEvent of(String... userId) {

        return of(userId == null ? null : Arrays.asList(userId));
    }
}
