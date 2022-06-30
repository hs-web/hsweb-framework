package org.hswebframework.web.system.authorization.api.event;

import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

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
public class ClearUserAuthorizationCacheEvent extends DefaultAsyncEvent {
    private Set<String> userId;

    private boolean all;

    private boolean async;

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

    //兼容async
    public ClearUserAuthorizationCacheEvent useAsync() {
        this.async = true;
        return this;
    }

    @Override
    public Mono<Void> publish(ApplicationEventPublisher eventPublisher) {
        this.async = true;
        return super.publish(eventPublisher);
    }

    public static ClearUserAuthorizationCacheEvent of(String... userId) {

        return of(userId == null ? null : Arrays.asList(userId));
    }
}
