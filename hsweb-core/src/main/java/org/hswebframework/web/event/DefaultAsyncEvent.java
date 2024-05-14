package org.hswebframework.web.event;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class DefaultAsyncEvent implements AsyncEvent {

    private transient Mono<?> async = Mono.empty();
    private transient Mono<?> first = Mono.empty();

    private transient boolean hasListener;

    public synchronized void async(Publisher<?> publisher) {
        hasListener = true;
        this.async = async.then(AsyncEventHooks.hookAsync(this, Mono.fromDirect(publisher)));
    }

    @Override
    public synchronized void first(Publisher<?> publisher) {
        hasListener = true;
        this.first = AsyncEventHooks.hookFirst(this, Mono.fromDirect(publisher)).then(first);
    }

    @Override
    public synchronized void transformFirst(Function<Mono<?>, Publisher<?>> mapper) {
        hasListener = true;
        this.first = Mono.fromDirect(mapper.apply(this.first));
    }

    @Override
    public synchronized void transform(Function<Mono<?>, Publisher<?>> mapper) {
        hasListener = true;
        this.async = Mono.fromDirect(mapper.apply(this.async));
    }

    @Override
    public Mono<Void> getAsync() {
        return this.first.then(this.async).then();
    }

    @Override
    public Mono<Void> publish(ApplicationEventPublisher eventPublisher) {

        eventPublisher.publishEvent(this);

        return getAsync();
    }

    public boolean hasListener() {
        return hasListener;
    }
}
