package org.hswebframework.web.datasource.switcher;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import static org.junit.Assert.*;

public class DefaultReactiveSwitcherTest {
    ReactiveSwitcher switcher = new DefaultReactiveSwitcher("test","datasource");


    @Test
    public void test() {

        switcher.use(getId(), "test")
                .as(StepVerifier::create)
                .expectNext("test")
                .verifyComplete();


        switcher.useDefault(getId())
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();

    }

    public Mono<String> getId() {
        return Mono.just(1)
                .zipWith(switcher.current())
                .map(Tuple2::getT2);
    }
}