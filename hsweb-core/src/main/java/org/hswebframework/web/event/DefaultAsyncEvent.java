package org.hswebframework.web.event;

import lombok.Getter;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

public class DefaultAsyncEvent implements AsyncEvent {

    @Getter
    private Mono<Void> async = Mono.empty();
    @Getter
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
    public Mono<Void> publish(ApplicationEventPublisher eventPublisher) {

        eventPublisher.publishEvent(this);

        return this.first.then(this.async);
    }

    public boolean hasListener() {
        return hasListener;
    }
}
