package org.hswebframework.web.event;

import lombok.Getter;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

public class DefaultAsyncEvent implements AsyncEvent {

    @Getter
    private Mono<Void> async = Mono.empty();

    public synchronized void async(Publisher<?> publisher) {
        this.async = async.then(Mono.from(publisher).then());
    }

    @Override
    public Mono<Void> publish(ApplicationEventPublisher eventPublisher) {

        eventPublisher.publishEvent(this);

        return this.async;
    }
}
