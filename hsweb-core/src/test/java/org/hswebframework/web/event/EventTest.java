package org.hswebframework.web.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class EventTest {

    @Test
    public void testMonoFrom() {
        Flux<String> source =  Flux.just("1", "2", "3").doOnNext(s -> log.info("get {}", s));

        Mono.from(source).subscribe();

        Mono.fromDirect(source).subscribe();
    }

    @Test
    public void testAsync() {
        Flux<String> source =  Flux.just("1", "2", "3").doOnNext(s -> log.info("get {}", s));
        AsyncEvent event = new DefaultAsyncEvent();
        event.async(source);
        event.getAsync().subscribe();
    }
}
