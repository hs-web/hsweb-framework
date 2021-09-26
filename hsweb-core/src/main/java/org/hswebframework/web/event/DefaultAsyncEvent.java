package org.hswebframework.web.event;

import lombok.Getter;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

public class DefaultAsyncEvent implements AsyncEvent {

    private Mono<Void> async = Mono.empty();
    private Mono<Void> first = Mono.empty();

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
    public Mono<Void> getAsync() {
        return this.first.then(this.async);
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
