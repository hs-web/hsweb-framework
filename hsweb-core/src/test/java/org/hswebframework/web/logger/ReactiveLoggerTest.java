package org.hswebframework.web.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.Assert.*;

@Slf4j
public class ReactiveLoggerTest {


    @Test
    public void test() {

        Flux.range(0, 5)
                .flatMap(i -> ReactiveLogger.mdc("requestId", "test").thenReturn(i))
                .doOnEach(ReactiveLogger.onNext(v -> {

                    log.info("test:{} {}", v, MDC.getCopyOfContextMap());
                }))
                .contextWrite(ReactiveLogger.start("r", "1","t","1"))
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();


    }

    @Test
    public void testHandle() {
        Flux.range(0, 5)
                .delayElements(Duration.ofSeconds(1))
                .flatMap(i -> ReactiveLogger.mdc("requestId", "test").thenReturn(i))
                .handle(ReactiveLogger.handle((o, fluxSink) -> {
                    log.info("test:{}", fluxSink.currentContext());
                    fluxSink.next(o);
                })).contextWrite(ReactiveLogger.start("r", "1"))
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

    }
}