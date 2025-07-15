package org.hswebframework.web.id;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class RandomIdGeneratorTest {


    @Test
    public void test() {
        RandomIdGenerator.GLOBAL.generate();

        long now = System.currentTimeMillis();
        String id = RandomIdGenerator.GLOBAL.generate();
        System.out.println(id + "-->" + id.length());
        long ts = RandomIdGenerator.getTimestampInId(id);

        System.out.println(now + ">" + ts);
        assertTrue(RandomIdGenerator.isRandomId(id));
        assertTrue(RandomIdGenerator.timestampRangeOf(id, Duration.ofMillis(100)));
        assertTrue(ts >= now);
    }

    @Test
    public void thread() {
        int size = 10_0000;

        Duration duration = Flux
            .range(0, size)
            .flatMap(i -> Flux.merge(
                Mono.fromSupplier(RandomIdGenerator.GLOBAL::generate)
                    .subscribeOn(Schedulers.parallel())
                ,
                Mono.fromSupplier(RandomIdGenerator.GLOBAL::generate)
                    .subscribeOn(Schedulers.boundedElastic())
            ))
            .distinct()
            .as(StepVerifier::create)
            .expectNextCount(size * 2)
            .verifyComplete();

        System.out.println(duration);
    }
}