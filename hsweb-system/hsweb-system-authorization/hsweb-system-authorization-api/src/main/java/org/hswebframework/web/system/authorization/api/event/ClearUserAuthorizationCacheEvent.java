package org.hswebframework.web.system.authorization.api.event;

import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

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

    private static final String DISABLE_KEY = ClearUserAuthorizationCacheEvent.class + "_Disabled";

    public static <T> Flux<T> disable(Flux<T> task) {
        return task.contextWrite(Context.of(DISABLE_KEY, true));
    }

    public static <T> Mono<T> disable(Mono<T> task) {
        return task.contextWrite(Context.of(DISABLE_KEY, true));
    }

    public static Mono<Void> doOnEnabled(Mono<Void> task) {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(DISABLE_KEY)) {
                return Mono.empty();
            }
            return task;
        });
    }

    @Override
    public synchronized void async(Publisher<?> publisher) {
        super.async(doOnEnabled(Mono.fromDirect(publisher).then()));
    }

    @Override
    public synchronized void first(Publisher<?> publisher) {
        super.first(doOnEnabled(Mono.fromDirect(publisher).then()));
    }

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
