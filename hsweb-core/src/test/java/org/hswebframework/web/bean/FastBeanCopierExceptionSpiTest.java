package org.hswebframework.web.bean;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FastBeanCopierExceptionSpiTest {

    @Test
    public void serviceLoaderCustomizesDetailAndKeepsFilteringLazy() throws Exception {
        withProviders(CustomConverter.class.getName(), isolated -> {
            CustomConverter.detailReads = 0;
            Method copy = isolated.getMethod("copy", Object.class, Object.class, String[].class);
            Throwable error = new IllegalStateException("failed");

            Map<String, Object> filtered = new LinkedHashMap<>();
            Assert.assertSame(filtered, copy.invoke(null, error, filtered, new String[]{"message", "detail"}));
            Assert.assertEquals(Map.of("type", error.getClass().getName(), "extra", "custom"), filtered);
            Assert.assertEquals(0, CustomConverter.detailReads);

            Map<String, Object> full = new LinkedHashMap<>();
            copy.invoke(null, error, full, new String[0]);
            Assert.assertEquals("compact:failed", full.get("detail"));
            Assert.assertEquals("custom", full.get("extra"));
            Assert.assertEquals("failed", full.get("message"));
            Assert.assertEquals(1, CustomConverter.detailReads);

            Method restore = isolated.getMethod("copy", Object.class, Class.class, String[].class);
            Throwable remote = (Throwable) restore.invoke(null,
                Map.of("type", "remote.Foreign", "message", "remote", "detail", "original remote stack"),
                Throwable.class, new String[0]);
            Map<String, Object> forwarded = new LinkedHashMap<>();
            copy.invoke(null, remote, forwarded, new String[0]);
            Assert.assertEquals("remote.Foreign", forwarded.get("type"));
            Assert.assertEquals("original remote stack", forwarded.get("detail"));
            Assert.assertEquals(1, CustomConverter.detailReads);
            Throwable restored = (Throwable) restore.invoke(null, Map.of("type", "remote.Custom", "message", "restored"),
                                                            CustomException.class, new String[0]);
            Assert.assertTrue(restored instanceof CustomException);
            Assert.assertEquals("restored", restored.getMessage());
            for (String invalid : new String[]{"wrongType", "nullResult"}) {
                InvocationTargetException failure = Assert.assertThrows(InvocationTargetException.class,
                    () -> restore.invoke(null, Map.of("type", "remote.Custom", invalid, true),
                                         CustomException.class, new String[0]));
                Assert.assertTrue(failure.getCause() instanceof UnsupportedOperationException);
            }
        });
    }

    @Test
    public void coldConcurrentInitializationCreatesOnlyTheFirstProvider() throws Exception {
        FirstConverter.constructions.set(0);
        SecondConverter.constructions.set(0);
        withProviders(FirstConverter.class.getName() + "\n" + SecondConverter.class.getName(), isolated -> {
            Method copy = isolated.getMethod("copy", Object.class, Object.class, String[].class);
            ExecutorService workers = Executors.newFixedThreadPool(8);
            CountDownLatch start = new CountDownLatch(1);
            try {
                List<Future<?>> tasks = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    tasks.add(workers.submit(() -> {
                        start.await();
                        Map<String, Object> result = new LinkedHashMap<>();
                        copy.invoke(null, new IllegalStateException("failed"), result, new String[]{"message", "detail"});
                        Assert.assertEquals(Map.of("type", IllegalStateException.class.getName(), "provider", "first"), result);
                        return null;
                    }));
                }
                start.countDown();
                for (Future<?> task : tasks) {
                    task.get(15, TimeUnit.SECONDS);
                }
                Assert.assertEquals(1, FirstConverter.constructions.get());
                Assert.assertEquals(0, SecondConverter.constructions.get());
            } finally {
                start.countDown();
                workers.shutdownNow();
                Assert.assertTrue(workers.awaitTermination(15, TimeUnit.SECONDS));
            }
        });
    }

    @Test
    public void invalidServiceAndConstructorFailureStayVisibleWithoutBreakingOrdinaryCopy() throws Exception {
        for (String provider : List.of("missing.ExceptionConverter", BrokenConstructor.class.getName())) {
            withProviders(provider, isolated -> {
                Method copy = isolated.getMethod("copy", Object.class, Object.class, String[].class);
                InvocationTargetException first = Assert.assertThrows(InvocationTargetException.class,
                    () -> copy.invoke(null, new IllegalStateException("failed"), new LinkedHashMap<>(), new String[0]));
                Assert.assertTrue(first.getCause() instanceof ServiceConfigurationError);
                InvocationTargetException again = Assert.assertThrows(InvocationTargetException.class,
                    () -> copy.invoke(null, new IllegalStateException("failed"), new LinkedHashMap<>(), new String[0]));
                Assert.assertTrue(again.getCause() instanceof NoClassDefFoundError);
                Map<String, Object> ordinary = new LinkedHashMap<>();
                copy.invoke(null, Map.of("value", 12), ordinary, new String[0]);
                Assert.assertEquals(Map.of("value", 12), ordinary);
                Method typed = isolated.getMethod("copy", Object.class, Class.class, String[].class);
                Throwable live = new IllegalStateException("live");
                Assert.assertSame(live, typed.invoke(null, live, Throwable.class, new String[0]));
            });
        }
    }

    @Test
    public void providerMappingErrorPropagatesButDoesNotPoisonTheInitializedProvider() throws Exception {
        withProviders(BrokenMapping.class.getName(), isolated -> {
            Method typed = isolated.getMethod("copy", Object.class, Class.class, String[].class);
            InvocationTargetException failure = Assert.assertThrows(InvocationTargetException.class,
                () -> typed.invoke(null, Map.of("type", "remote.Error"), Throwable.class, new String[0]));
            Assert.assertSame(BrokenMapping.failure, failure.getCause());
            Method copy = isolated.getMethod("copy", Object.class, Object.class, String[].class);
            Map<String, Object> result = new LinkedHashMap<>();
            copy.invoke(null, new IllegalStateException("still usable"), result, new String[]{"detail"});
            Assert.assertEquals("still usable", result.get("message"));
        });
    }

    private void withProviders(String providers, Check check) throws Exception {
        Path root = Files.createTempDirectory("exception-map-spi");
        String serviceName = "META-INF/services/" + ExceptionConverter.class.getName();
        Path service = root.resolve(serviceName);
        Files.createDirectories(service.getParent());
        Files.writeString(service, providers);
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        String copierName = FastBeanCopier.class.getName();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{root.toUri().toURL()}, getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.equals(copierName) && !name.startsWith(copierName + "$")) {
                    return super.loadClass(name, resolve);
                }
                Class<?> type = findLoadedClass(name);
                if (type == null) {
                    try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (input == null) {
                            throw new ClassNotFoundException(name);
                        }
                        byte[] bytes = input.readAllBytes();
                        type = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                if (resolve) {
                    resolveClass(type);
                }
                return type;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return serviceName.equals(name) ? findResources(name) : super.getResources(name);
            }
        }) {
            thread.setContextClassLoader(loader);
            check.accept(Class.forName(copierName, true, loader));
        } finally {
            thread.setContextClassLoader(original);
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            }
        }
    }

    private interface Check {
        void accept(Class<?> copier) throws Exception;
    }

    public static class CustomConverter extends DefaultExceptionConverter {
        static int detailReads;

        @Override
        public void toMap(Throwable source, BiConsumer<String, Supplier<?>> properties) {
            super.toMap(source, properties);
            properties.accept("extra", () -> "custom");
        }

        @Override
        protected String getDetail(Throwable source) {
            detailReads++;
            return "compact:" + source.getMessage();
        }

        @Override
        public Throwable fromMap(Map<String, ?> source, Class<? extends Throwable> targetType) {
            if (source.containsKey("wrongType")) return new AssertionError("wrong type");
            if (source.containsKey("nullResult")) return null;
            if (targetType == CustomException.class) return new CustomException((String) source.get("message"));
            return super.fromMap(source, targetType);
        }
    }

    public static class CustomException extends RuntimeException {
        public CustomException(String message) { super(message); }
    }

    public static class FirstConverter extends DefaultExceptionConverter {
        static final AtomicInteger constructions = new AtomicInteger();
        public FirstConverter() { constructions.incrementAndGet(); }
        @Override
        public void toMap(Throwable source, BiConsumer<String, Supplier<?>> properties) {
            super.toMap(source, properties);
            properties.accept("provider", () -> "first");
        }
    }

    public static class SecondConverter extends DefaultExceptionConverter {
        static final AtomicInteger constructions = new AtomicInteger();
        public SecondConverter() { constructions.incrementAndGet(); }
    }

    public static class BrokenConstructor extends DefaultExceptionConverter {
        public BrokenConstructor() { throw new IllegalStateException("constructor failed"); }
    }

    public static class BrokenMapping extends DefaultExceptionConverter {
        static final AssertionError failure = new AssertionError("mapping failed");
        @Override
        public Throwable fromMap(Map<String, ?> source, Class<? extends Throwable> targetType) { throw failure; }
    }
}
