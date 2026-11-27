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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FastBeanCopierExceptionSpiTest {

    @Test
    public void serviceLoaderCustomizesDetailAndKeepsFilteringLazy() throws Exception {
        Path root = Files.createTempDirectory("exception-map-spi");
        String serviceName = "META-INF/services/" + ExceptionConverter.class.getName();
        Path service = root.resolve(serviceName);
        Files.createDirectories(service.getParent());
        Files.writeString(service, CustomConverter.class.getName());
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
            CustomConverter.detailReads = 0;
            Class<?> isolated = Class.forName(copierName, true, loader);
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
        } finally {
            thread.setContextClassLoader(original);
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            }
        }
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
            if (source.containsKey("wrongType")) {
                return new AssertionError("wrong type");
            }
            if (source.containsKey("nullResult")) {
                return null;
            }
            if (targetType == CustomException.class) {
                return new CustomException((String) source.get("message"));
            }
            return super.fromMap(source, targetType);
        }
    }

    public static class CustomException extends RuntimeException {
        public CustomException(String message) {
            super(message);
        }
    }
}
