package org.hswebframework.web.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import org.hswebframework.web.cache.supports.CaffeineReactiveCache;
import org.hswebframework.web.cache.supports.GuavaReactiveCache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class LocalReactiveCacheInvalidationTest {

    private static final String KEY = "subject";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> backends() {
        return Arrays.asList(new Object[]{"guava"}, new Object[]{"caffeine"});
    }

    private final String backend;

    public LocalReactiveCacheInvalidationTest(String backend) {
        this.backend = backend;
    }

    @Test
    public void evictionPreventsOldLoadFromRepopulatingCache() {
        ReactiveCache<String> cache = createCache();
        assertOldLoadCannotRepopulate(cache, cache.evict(KEY));
    }

    @Test
    public void batchEvictionPreventsOldLoadFromRepopulatingCache() {
        ReactiveCache<String> cache = createCache();
        assertOldLoadCannotRepopulate(cache, cache.evictAll(Collections.singleton(KEY)));
    }

    @Test
    public void clearPreventsOldLoadFromRepopulatingCache() {
        ReactiveCache<String> cache = createCache();
        assertOldLoadCannotRepopulate(cache, cache.clear());
    }

    private static void assertOldLoadCannotRepopulate(ReactiveCache<String> cache,
                                                      Mono<Void> invalidation) {
        Sinks.One<String> pending = Sinks.one();
        AtomicReference<String> oldResult = new AtomicReference<>();
        cache.getMono(KEY, pending::asMono).subscribe(oldResult::set);
        invalidation.as(StepVerifier::create).verifyComplete();
        cache.getMono(KEY)
             .as(StepVerifier::create)
             .expectComplete()
             .verify(TIMEOUT);
        pending.emitValue("A", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("A", oldResult.get());
        assertFreshValue(cache);
    }

    @Test
    public void existingSubscribersCanCompleteAfterInvalidation() {
        ReactiveCache<String> cache = createCache();
        Sinks.One<String> pending = Sinks.one();
        AtomicReference<String> firstResult = new AtomicReference<>();
        AtomicReference<String> sharedResult = new AtomicReference<>();
        cache.getMono(KEY, pending::asMono).subscribe(firstResult::set);
        cache.getMono(KEY).subscribe(sharedResult::set);
        cache.evict(KEY).as(StepVerifier::create).verifyComplete();
        pending.emitValue("A", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("A", firstResult.get());
        Assert.assertEquals("A", sharedResult.get());
        assertFreshValue(cache);
    }

    @Test
    public void oldCompletionAfterClearDoesNotRemoveNewLoader() {
        ReactiveCache<String> cache = createCache();
        Sinks.One<String> oldLoad = Sinks.one();
        Sinks.One<String> newLoad = Sinks.one();
        AtomicReference<String> oldResult = new AtomicReference<>();
        AtomicReference<String> newResult = new AtomicReference<>();
        cache.getMono(KEY, oldLoad::asMono).subscribe(oldResult::set);
        cache.clear().as(StepVerifier::create).verifyComplete();
        cache.getMono(KEY, newLoad::asMono).subscribe(newResult::set);

        oldLoad.emitValue("A", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("A", oldResult.get());
        newLoad.emitValue("B", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("B", newResult.get());
        cache.getMono(KEY, () -> Mono.error(new AssertionError("new loader was detached")))
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
    }

    @Test
    public void currentLoaderErrorAllowsReloadAndHotHit() {
        ReactiveCache<String> cache = createCache();
        cache.getMono(KEY, () -> Mono.error(new IllegalStateException("current loader failed")))
             .as(StepVerifier::create)
             .expectComplete()
             .verify(TIMEOUT);

        AtomicInteger freshLoads = new AtomicInteger();
        Supplier<Mono<String>> fresh = () -> Mono.defer(() -> {
            freshLoads.incrementAndGet();
            return Mono.just("B");
        });
        cache.getMono(KEY, fresh)
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        cache.getMono(KEY, fresh)
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        Assert.assertEquals(1, freshLoads.get());
    }

    @Test
    public void oldErrorDoesNotRemoveNewPendingLoader() {
        ReactiveCache<String> cache = createCache();
        Sinks.One<String> oldLoad = Sinks.one();
        Sinks.One<String> newLoad = Sinks.one();
        AtomicInteger freshLoads = new AtomicInteger();
        AtomicInteger oldCompleted = new AtomicInteger();
        AtomicReference<Throwable> oldFailure = new AtomicReference<>();
        AtomicReference<String> newResult = new AtomicReference<>();
        cache.getMono(KEY, oldLoad::asMono)
             .subscribe(ignored -> { }, oldFailure::set, oldCompleted::incrementAndGet);
        cache.evict(KEY).as(StepVerifier::create).verifyComplete();
        Supplier<Mono<String>> fresh = () -> Mono.defer(() -> {
            freshLoads.incrementAndGet();
            return newLoad.asMono();
        });
        cache.getMono(KEY, fresh).subscribe(newResult::set);
        oldLoad.emitError(new IllegalStateException("old loader failed"), Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertNull(oldFailure.get());
        Assert.assertEquals(1, oldCompleted.get());
        newLoad.emitValue("B", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("B", newResult.get());
        cache.getMono(KEY, fresh)
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        Assert.assertEquals(1, freshLoads.get());
    }

    @Test
    public void oldErrorDoesNotRemoveCompletedNewValue() {
        ReactiveCache<String> cache = createCache();
        Sinks.One<String> oldLoad = Sinks.one();
        AtomicInteger freshLoads = new AtomicInteger();
        AtomicInteger oldCompleted = new AtomicInteger();
        AtomicReference<Throwable> oldFailure = new AtomicReference<>();
        cache.getMono(KEY, oldLoad::asMono)
             .subscribe(ignored -> { }, oldFailure::set, oldCompleted::incrementAndGet);
        cache.evict(KEY).as(StepVerifier::create).verifyComplete();
        Supplier<Mono<String>> fresh = () -> Mono.defer(() -> {
            freshLoads.incrementAndGet();
            return Mono.just("B");
        });
        cache.getMono(KEY, fresh)
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        oldLoad.emitError(new IllegalStateException("old loader failed"), Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertNull(oldFailure.get());
        Assert.assertEquals(1, oldCompleted.get());
        cache.getMono(KEY, fresh)
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        Assert.assertEquals(1, freshLoads.get());
    }

    @Test
    public void monoAssembledDuringClearDoesNotCaptureOldValue() throws Exception {
        assertAssemblingDuringClearDoesNotCaptureOldValue(false);
    }

    @Test
    public void fluxAssembledDuringClearDoesNotCaptureOldValue() throws Exception {
        assertAssemblingDuringClearDoesNotCaptureOldValue(true);
    }

    private void assertAssemblingDuringClearDoesNotCaptureOldValue(boolean flux) throws Exception {
        CountDownLatch clearWindow = new CountDownLatch(1);
        CountDownLatch releaseClear = new CountDownLatch(1);
        ReactiveCache<String> cache = createCache(() -> { }, () -> {
            clearWindow.countDown();
            awaitRelease(releaseClear);
        });
        cache.put(KEY, flux ? Flux.just("A") : Mono.just("A")).block(TIMEOUT);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> clear = executor.submit(() -> cache.clear().block(TIMEOUT));
            Assert.assertTrue(clearWindow.await(5, TimeUnit.SECONDS));
            // 此时加载映射已清理，后端 invalidateAll 尚未执行；仅装配，不订阅。
            Publisher<String> assembled = flux ? cache.getFlux(KEY) : cache.getMono(KEY);
            Assert.assertNotNull(assembled);
            releaseClear.countDown();
            clear.get(5, TimeUnit.SECONDS);
            if (flux) {
                cache.getFlux(KEY).as(StepVerifier::create).expectComplete().verify(TIMEOUT);
            } else {
                cache.getMono(KEY).as(StepVerifier::create).expectComplete().verify(TIMEOUT);
            }
        } finally {
            releaseClear.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void clearPreventsOldFluxLoadFromRepopulatingCache() {
        ReactiveCache<String> cache = createCache();
        Sinks.One<String> pending = Sinks.one();
        AtomicReference<String> oldResult = new AtomicReference<>();
        cache.getFlux(KEY, () -> pending.asMono().flux()).subscribe(oldResult::set);
        cache.clear().as(StepVerifier::create).verifyComplete();
        pending.emitValue("A", Sinks.EmitFailureHandler.FAIL_FAST);
        Assert.assertEquals("A", oldResult.get());
        cache.getFlux(KEY, () -> Flux.just("B"))
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        cache.getFlux(KEY, () -> Flux.error(new AssertionError("new flux was not cached")))
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
    }

    @Test
    public void invalidationCannotOvertakeAnAtomicWriteBack() throws Exception {
        assertInvalidationCannotOvertakeWriteBack(false);
    }

    @Test
    public void clearCannotOvertakeAnAtomicWriteBack() throws Exception {
        assertInvalidationCannotOvertakeWriteBack(true);
    }

    private void assertInvalidationCannotOvertakeWriteBack(boolean clear) throws Exception {
        CountDownLatch writeEntered = new CountDownLatch(1);
        CountDownLatch releaseWrite = new CountDownLatch(1);
        CountDownLatch invalidationStarted = new CountDownLatch(1);
        CountDownLatch invalidationCompleted = new CountDownLatch(1);
        ReactiveCache<String> cache = createCache(() -> {
            writeEntered.countDown();
            try {
                if (!releaseWrite.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("write was not released");
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(interrupted);
            }
        });
        Sinks.One<String> pending = Sinks.one();
        AtomicReference<String> oldResult = new AtomicReference<>();
        cache.getMono(KEY, pending::asMono).subscribe(oldResult::set);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> oldCompletion = executor.submit(() ->
                pending.emitValue("A", Sinks.EmitFailureHandler.FAIL_FAST));
            Assert.assertTrue(writeEntered.await(5, TimeUnit.SECONDS));
            Future<?> invalidation = executor.submit(() -> {
                invalidationStarted.countDown();
                (clear ? cache.clear() : cache.evict(KEY)).block(TIMEOUT);
                invalidationCompleted.countDown();
            });
            Assert.assertTrue(invalidationStarted.await(5, TimeUnit.SECONDS));
            Assert.assertFalse(invalidationCompleted.await(100, TimeUnit.MILLISECONDS));
            releaseWrite.countDown();
            oldCompletion.get(5, TimeUnit.SECONDS);
            invalidation.get(5, TimeUnit.SECONDS);
            Assert.assertEquals("A", oldResult.get());
            assertFreshValue(cache);
        } finally {
            releaseWrite.countDown();
            executor.shutdownNow();
        }
    }

    private static void assertFreshValue(ReactiveCache<String> cache) {
        AtomicInteger freshLoads = new AtomicInteger();
        cache.getMono(KEY, () -> Mono.defer(() -> {
            freshLoads.incrementAndGet();
            return Mono.just("B");
        }))
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
        Assert.assertEquals(1, freshLoads.get());
        cache.getMono(KEY, () -> Mono.error(new AssertionError("fresh value was not cached")))
             .as(StepVerifier::create)
             .expectNext("B")
             .verifyComplete();
    }

    private ReactiveCache<String> createCache() {
        return createCache(() -> { });
    }

    private ReactiveCache<String> createCache(Runnable beforeOldWrite) {
        return createCache(beforeOldWrite, null);
    }

    private ReactiveCache<String> createCache(Runnable beforeOldWrite, Runnable beforeClear) {
        if ("guava".equals(backend)) {
            com.google.common.cache.Cache<Object, Object> nativeCache = CacheBuilder.newBuilder().build();
            com.google.common.cache.Cache<Object, Object> cache = beforeClear == null
                ? nativeCache
                : observeClear(com.google.common.cache.Cache.class, nativeCache, beforeClear);
            return new GuavaReactiveCache<String>(cache) {
                @Override
                public Mono<Void> putNow(Object key, Object value) {
                    if ("A".equals(value)) {
                        beforeOldWrite.run();
                    }
                    return super.putNow(key, value);
                }
            };
        }
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = Caffeine.newBuilder().build();
        com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = beforeClear == null
            ? nativeCache
            : observeClear(com.github.benmanes.caffeine.cache.Cache.class, nativeCache, beforeClear);
        return new CaffeineReactiveCache<String>(cache) {
            @Override
            public Mono<Void> putNow(Object key, Object value) {
                if ("A".equals(value)) {
                    beforeOldWrite.run();
                }
                return super.putNow(key, value);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> T observeClear(Class<?> cacheType, T cache, Runnable beforeClear) {
        return (T) Proxy.newProxyInstance(cacheType.getClassLoader(), new Class<?>[]{cacheType},
            (proxy, method, args) -> {
                if ("invalidateAll".equals(method.getName()) && method.getParameterCount() == 0) {
                    beforeClear.run();
                }
                try {
                    return method.invoke(cache, args);
                } catch (InvocationTargetException invocation) {
                    throw invocation.getCause();
                }
            });
    }

    private static void awaitRelease(CountDownLatch release) {
        try {
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("operation was not released");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
