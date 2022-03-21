package org.hswebframework.web.event;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class DefaultAsyncEvent implements AsyncEvent {

    private Mono<?> async = Mono.empty();
    private Mono<?> first = Mono.empty();

    private boolean hasListener;

    public synchronized void async(Publisher<?> publisher) {
        hasListener = true;
        this.async = async.then(Mono.from(publisher).then());
    }

    @Override
    public synchronized void first(Publisher<?> publisher) {
        hasListener = true;
        this.first = Mono.from(publisher).then(first);
    }

    @Override
    public void first(Function<Mono<?>, Publisher<?>> mapper) {
        this.first = Mono.from(mapper.apply(this.first));
    }

    @Override
    public void async(Function<Mono<?>, Publisher<?>> mapper) {
        this.async = Mono.from(mapper.apply(this.async));
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
