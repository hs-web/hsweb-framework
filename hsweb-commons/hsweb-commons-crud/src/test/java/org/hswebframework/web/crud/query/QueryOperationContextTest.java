package org.hswebframework.web.crud.query;

import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class QueryOperationContextTest {

    private static final QueryOperationContext.Key<Integer> KEY =
        new QueryOperationContext.Key<>(Integer.class, "metadata");

    @Test
    public void testConcurrentSubscribersShareResolutionAndCancellation() {
        QueryOperationContext context = new QueryOperationContext();
        AtomicInteger subscribed = new AtomicInteger();
        AtomicInteger cancelled = new AtomicInteger();
        Mono<Integer> resolution = context.memoize(KEY, () -> Mono
            .<Integer>never()
            .doOnSubscribe(ignore -> subscribed.incrementAndGet())
            .doOnCancel(cancelled::incrementAndGet));

        Disposable first = resolution.subscribe();
        Disposable second = resolution.subscribe();
        assertEquals(1, subscribed.get());

        first.dispose();
        assertEquals(0, cancelled.get());
        second.dispose();
        assertEquals(1, cancelled.get());

        Disposable later = resolution.subscribe();
        assertEquals(2, subscribed.get());
        later.dispose();
        assertEquals(2, cancelled.get());
    }

    @Test
    public void testSuccessfulResultIsReusedForEqualTypedKey() {
        QueryOperationContext context = new QueryOperationContext();
        AtomicInteger loaded = new AtomicInteger();
        Mono<Integer> resolution = context.memoize(KEY, () -> Mono.fromSupplier(loaded::incrementAndGet));

        StepVerifier.create(resolution).expectNext(1).verifyComplete();
        StepVerifier.create(context.memoize(
            new QueryOperationContext.Key<>(Integer.class, "metadata"),
            () -> Mono.error(new AssertionError("equal key must reuse its first loader"))))
            .expectNext(1)
            .verifyComplete();

        assertEquals(1, loaded.get());
    }

    @Test
    public void testEmptyResultCompletesEmptyAndCanResolveAgain() {
        QueryOperationContext context = new QueryOperationContext();
        AtomicInteger loaded = new AtomicInteger();
        Mono<Integer> resolution = context.memoize(KEY, () -> loaded.incrementAndGet() == 1
            ? Mono.empty()
            : Mono.just(2));

        StepVerifier.create(resolution).verifyComplete();
        StepVerifier.create(resolution).expectNext(2).verifyComplete();
        StepVerifier.create(resolution).expectNext(2).verifyComplete();
        assertEquals(2, loaded.get());
    }

    @Test
    public void testErrorPropagatesAndCanResolveAgain() {
        QueryOperationContext context = new QueryOperationContext();
        AtomicInteger loaded = new AtomicInteger();
        IllegalStateException failure = new IllegalStateException("metadata unavailable");
        Mono<Integer> resolution = context.memoize(KEY, () -> loaded.incrementAndGet() == 1
            ? Mono.error(failure)
            : Mono.just(2));

        StepVerifier.create(resolution).verifyErrorSatisfies(error -> assertSame(failure, error));
        StepVerifier.create(resolution).expectNext(2).verifyComplete();
        assertEquals(2, loaded.get());
    }

    @Test
    public void testSubscriptionsCreateFreshScopeAndPreserveReactorContext() {
        AtomicReference<QueryOperationContext> previous = new AtomicReference<>();
        AtomicInteger loaded = new AtomicInteger();
        Mono<Integer> operation = QueryOperationContext.within(Mono.deferContextual(context -> {
            assertEquals("permission", context.get("caller"));
            QueryOperationContext current = QueryOperationContext.current(context).orElseThrow();
            assertFalse(current == previous.getAndSet(current));
            return current.memoize(KEY, () -> Mono.deferContextual(loaderContext -> {
                assertEquals("permission", loaderContext.get("caller"));
                assertSame(current, QueryOperationContext.current(loaderContext).orElseThrow());
                return Mono.just(loaded.incrementAndGet());
            }));
        })).contextWrite(Context.of("caller", "permission"));

        StepVerifier.create(operation).expectNext(1).verifyComplete();
        StepVerifier.create(operation).expectNext(2).verifyComplete();
    }

    @Test
    public void testWithinJoinsExplicitlyEnclosingScope() {
        QueryOperationContext enclosing = new QueryOperationContext();
        Mono<Integer> operation = QueryOperationContext.within(Mono.deferContextual(context -> {
            assertSame(enclosing, QueryOperationContext.current(context).orElseThrow());
            return Mono.just(1);
        })).contextWrite(Context.of(QueryOperationContext.class, enclosing));

        StepVerifier.create(operation).expectNext(1).verifyComplete();
    }
}
