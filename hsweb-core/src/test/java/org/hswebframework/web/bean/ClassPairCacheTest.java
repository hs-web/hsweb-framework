package org.hswebframework.web.bean;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassPairCacheTest {
    @Test
    public void shouldKeepSourceAndTargetPairsDistinctAcrossSparseAndDenseRows() {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        Assert.assertNull(cache.get(Object.class, String.class));
        AtomicInteger creations = new AtomicInteger();
        for (Class<?> source : new Class[]{Object.class, String.class}) {
            for (int i = 1; i <= 20; i++) {
                Class<?> target = arrayType(i);
                Object value = cache.computeIfAbsent(source, target, (s, t) -> {
                    creations.incrementAndGet();
                    return new Object();
                });
                Assert.assertSame(value, cache.get(source, target));
                Assert.assertSame(value, cache.computeIfAbsent(source, target, (s, t) -> {
                    throw new AssertionError("cached pair must not be recreated");
                }));
            }
        }
        Assert.assertEquals(40, creations.get());
        Assert.assertEquals(40, cache.size());
        Assert.assertNotSame(cache.get(Object.class, arrayType(1)), cache.get(String.class, arrayType(1)));
    }

    @Test
    public void shouldPublishEachPairOnceUnderConcurrentCreationAndReads() throws Exception {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        AtomicInteger creations = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int thread = 0; thread < 8; thread++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int repeat = 0; repeat < 100; repeat++) {
                        for (int dimension = 1; dimension <= 20; dimension++) {
                            Class<?> target = arrayType(dimension);
                            Object value = cache.computeIfAbsent(Object.class, target, (s, t) -> {
                                creations.incrementAndGet();
                                return new Object();
                            });
                            Assert.assertSame(value, cache.get(Object.class, target));
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
            Assert.assertEquals(20, creations.get());
            Assert.assertEquals(20, cache.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void shouldAllowRecursiveCreationOfAnotherTargetForTheSameSource() {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        Object inner = new Object();
        Object outer = new Object();
        Assert.assertSame(outer, cache.computeIfAbsent(Object.class, String.class, (s, t) -> {
            Assert.assertSame(inner, cache.computeIfAbsent(s, Integer.class, (otherSource, otherTarget) -> inner));
            return outer;
        }));
        Assert.assertSame(inner, cache.get(Object.class, Integer.class));
        Assert.assertSame(outer, cache.get(Object.class, String.class));
        Assert.assertEquals(2, cache.size());
    }

    @Test
    public void shouldNotCacheNullOrFactoryFailure() {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        IllegalStateException failure = new IllegalStateException("failed");
        Assert.assertSame(failure, Assert.assertThrows(IllegalStateException.class,
            () -> cache.computeIfAbsent(Object.class, String.class, (s, t) -> { throw failure; })));
        Assert.assertEquals(0, cache.size());
        Assert.assertNull(cache.computeIfAbsent(Object.class, String.class, (s, t) -> null));
        Object value = new Object();
        Assert.assertSame(value, cache.computeIfAbsent(Object.class, String.class, (s, t) -> value));
        Assert.assertNull(cache.computeIfAbsent(Object.class, Integer.class, (s, t) -> null));
        Assert.assertSame(value, cache.get(Object.class, String.class));
        Assert.assertEquals(1, cache.size());
    }

    @Test
    public void shouldCreateDependentPairsAcrossSourcesWithoutHoldingRowLocks() throws Exception {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch entered = new CountDownLatch(2);
        try {
            Future<?> one = executor.submit(() -> cache.computeIfAbsent(Object.class, String.class, (s, t) -> {
                awaitBoth(entered);
                return cache.computeIfAbsent(Integer.class, Long.class, (childSource, childTarget) -> new Object());
            }));
            Future<?> two = executor.submit(() -> cache.computeIfAbsent(Integer.class, String.class, (s, t) -> {
                awaitBoth(entered);
                return cache.computeIfAbsent(Object.class, Long.class, (childSource, childTarget) -> new Object());
            }));
            Assert.assertNotNull(one.get(10, TimeUnit.SECONDS));
            Assert.assertNotNull(two.get(10, TimeUnit.SECONDS));
            Assert.assertEquals(4, cache.size());
        } finally {
            executor.shutdownNow();
        }
    }

    private static void awaitBoth(CountDownLatch entered) {
        entered.countDown();
        try {
            Assert.assertTrue(entered.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldRejectRecursiveCreationOfTheSamePair() {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        Assert.assertThrows(IllegalStateException.class,
            () -> cache.computeIfAbsent(Object.class, String.class,
                (s, t) -> cache.computeIfAbsent(s, t, (otherSource, otherTarget) -> new Object())));
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void shouldNotRepopulateClearedCacheFromAnInProgressFactory() throws Exception {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        URL location = ClassPairCacheTest.class.getProtectionDomain().getCodeSource().getLocation();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{location}, null)) {
            Class<?> target = loader.loadClass(LoaderType.class.getName());
            for (boolean full : new boolean[]{false, true}) {
                CountDownLatch entered = new CountDownLatch(1);
                CountDownLatch finish = new CountDownLatch(1);
                Object value = new Object();
                Future<Object> future = executor.submit(() -> cache.computeIfAbsent(Object.class, target, (s, t) -> {
                    entered.countDown();
                    try {
                        Assert.assertTrue(finish.await(5, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                    return value;
                }));
                Assert.assertTrue(entered.await(5, TimeUnit.SECONDS));
                if (full) {
                    cache.clear();
                } else {
                    cache.clear(loader);
                }
                finish.countDown();
                Assert.assertSame(value, future.get(10, TimeUnit.SECONDS));
                Assert.assertNull(cache.get(Object.class, target));
                Object recreated = cache.computeIfAbsent(Object.class, target, (s, t) -> new Object());
                Assert.assertNotSame(value, recreated);
                cache.clear();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void shouldClearMatchingSourcesAndTargetsAndPreserveOtherPairs() throws Exception {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        URL location = ClassPairCacheTest.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{location}, null)) {
            Class<?> dynamicType = loader.loadClass(LoaderType.class.getName());
            Object dynamicValue = cache.computeIfAbsent(Object.class, dynamicType, (s, t) -> new Object());
            cache.computeIfAbsent(dynamicType, String.class, (s, t) -> new Object());
            Object stableFirst = cache.computeIfAbsent(String.class, String.class, (s, t) -> new Object());
            cache.computeIfAbsent(String.class, dynamicType, (s, t) -> new Object());
            List<Object> stableValues = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                stableValues.add(cache.computeIfAbsent(Object.class, arrayType(i), (s, t) -> new Object()));
                cache.computeIfAbsent(String.class, arrayType(i), (s, t) -> new Object());
            }
            cache.clear(loader);
            Assert.assertNull(cache.get(Object.class, dynamicType));
            Assert.assertNull(cache.get(dynamicType, String.class));
            Assert.assertNull(cache.get(String.class, dynamicType));
            Assert.assertSame(stableFirst, cache.get(String.class, String.class));
            for (int i = 1; i <= 15; i++) {
                Assert.assertSame(stableValues.get(i - 1), cache.get(Object.class, arrayType(i)));
                Assert.assertNotNull(cache.get(String.class, arrayType(i)));
            }
            Assert.assertEquals(31, cache.size());
            Assert.assertNotSame(dynamicValue, cache.computeIfAbsent(Object.class, dynamicType, (s, t) -> new Object()));
            cache.clear(null);
            Assert.assertEquals(0, cache.size());
        }
    }

    @Test
    public void shouldDistinguishSameNamedClassesFromDifferentLoaders() throws Exception {
        ClassPairCache<Object> cache = new ClassPairCache<>();
        URL location = ClassPairCacheTest.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader first = new URLClassLoader(new URL[]{location}, null);
             URLClassLoader second = new URLClassLoader(new URL[]{location}, null)) {
            Class<?> firstType = first.loadClass(LoaderType.class.getName());
            Class<?> secondType = second.loadClass(LoaderType.class.getName());
            Object one = cache.computeIfAbsent(Object.class, firstType, (s, t) -> new Object());
            Object two = cache.computeIfAbsent(Object.class, secondType, (s, t) -> new Object());
            Assert.assertNotSame(one, two);
            cache.clear(first);
            Assert.assertNull(cache.get(Object.class, firstType));
            Assert.assertSame(two, cache.get(Object.class, secondType));
        }
    }

    private static Class<?> arrayType(int dimensions) {
        return Array.newInstance(String.class, new int[dimensions]).getClass();
    }

    public static class LoaderType {
    }
}
