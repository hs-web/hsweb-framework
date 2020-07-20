package org.hswebframework.web.event;

import lombok.Getter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Getter
public class AsyncEvent {

    private Mono<Void> async = Mono.empty();

    public synchronized void async(Publisher<?> publisher) {
        this.async = async.then(Mono.from(publisher).then());
    }

}
