package org.hswebframework.web.bean;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FastBeanCopierSupportTest {

    @Test
    public void testSupportCopy() {
        Source source = new Source();
        source.setName("support");
        source.setAge(18);
        source.setColor(Color.RED);

        Target target = FastBeanCopierSupport.copy(source, new Target());
        Target facadeTarget = FastBeanCopier.copy(source, new Target());

        Assert.assertEquals(facadeTarget.getName(), target.getName());
        Assert.assertEquals(facadeTarget.getAge(), target.getAge());
        Assert.assertEquals(facadeTarget.getColor(), target.getColor());
        Assert.assertEquals(facadeTarget.getColor2(), target.getColor2());
    }

    @Test
    public void testSupportGetProperty() {
        Source source = new Source();
        source.setAge(20);

        Assert.assertEquals(20, FastBeanCopierSupport.getProperty(source, "age"));
    }

    @Test
    public void testFacadeDelegatesToSupport() {
        Source source = new Source();
        Target target = new Target();

        Assert.assertNotSame(FastBeanCopierSupport.DEFAULT_CONVERT, FastBeanCopier.DEFAULT_CONVERT);
        Assert.assertEquals(
            FastBeanCopierSupport.DEFAULT_CONVERT.convert("123", Integer.class, FastBeanCopierSupport.EMPTY_CLASS_ARRAY),
            FastBeanCopier.DEFAULT_CONVERT.convert("123", Integer.class, FastBeanCopier.EMPTY_CLASS_ARRAY)
        );
        Assert.assertSame(FastBeanCopierSupport.getBackend(), FastBeanCopier.getBackend());
        Assert.assertTrue(FastBeanCopierSupport.getBackend() instanceof AsmAccessorFastBeanCopierBackend);
        Assert.assertSame(
            FastBeanCopierSupport.getCopier(source, target, true),
            FastBeanCopier.getCopier(source, target, true)
        );
    }


@Test
public void testDefaultBackendSelectorPrefersAsmOnJvm() {
    System.clearProperty(FastBeanCopierBackendSelector.BACKEND_PROPERTY);
    System.clearProperty(FastBeanCopierBackendSelector.NATIVE_HINT_PROPERTY);
    System.clearProperty(FastBeanCopierBackendSelector.NATIVE_IMAGE_PROPERTY);

    FastBeanCopierBackend backend = FastBeanCopierBackendSelector.selectDefaultBackend();
    Assert.assertTrue(backend instanceof AsmAccessorFastBeanCopierBackend);
}

@Test
public void testDefaultBackendSelectorFallsBackToReflectionForNativeHint() {
    String previous = System.getProperty(FastBeanCopierBackendSelector.NATIVE_HINT_PROPERTY);
    System.setProperty(FastBeanCopierBackendSelector.NATIVE_HINT_PROPERTY, "true");
    try {
        FastBeanCopierBackend backend = FastBeanCopierBackendSelector.selectDefaultBackend();
        Assert.assertTrue(backend instanceof ReflectionAccessorFastBeanCopierBackend);
    } finally {
        if (previous == null) {
            System.clearProperty(FastBeanCopierBackendSelector.NATIVE_HINT_PROPERTY);
        } else {
            System.setProperty(FastBeanCopierBackendSelector.NATIVE_HINT_PROPERTY, previous);
        }
    }
}

@Test
public void testBackendSelectorSupportsExplicitOverride() {
    String previous = System.getProperty(FastBeanCopierBackendSelector.BACKEND_PROPERTY);
    System.setProperty(FastBeanCopierBackendSelector.BACKEND_PROPERTY, "javassist");
    try {
        FastBeanCopierBackend backend = FastBeanCopierBackendSelector.selectDefaultBackend();
        Assert.assertTrue(backend instanceof JavassistFastBeanCopierBackend);
    } finally {
        if (previous == null) {
            System.clearProperty(FastBeanCopierBackendSelector.BACKEND_PROPERTY);
        } else {
            System.setProperty(FastBeanCopierBackendSelector.BACKEND_PROPERTY, previous);
        }
    }
}

    @Test
    public void testDefaultConverterSupportsGenericCollectionAndMapConversion() {
        java.util.List<?> list = FastBeanCopierSupport.DEFAULT_CONVERT.convert(
            java.util.Arrays.asList("1", "2", "3"),
            java.util.List.class,
            new Class[]{Integer.class}
        );
        Assert.assertEquals(java.util.Arrays.asList(1, 2, 3), list);

        java.util.Map<?, ?> map = FastBeanCopierSupport.DEFAULT_CONVERT.convert(
            java.util.Arrays.asList("alpha", "beta"),
            java.util.Map.class,
            new Class[]{Integer.class, String.class}
        );
        Assert.assertEquals("alpha", map.get(0));
        Assert.assertEquals("beta", map.get(1));

        java.util.List<?> second = FastBeanCopierSupport.DEFAULT_CONVERT.convert(
            new String[]{"4", "5"},
            java.util.List.class,
            new Class[]{Integer.class}
        );
        Assert.assertEquals(java.util.Arrays.asList(4, 5), second);
    }

    @Test
    public void testAdditionalBackendCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : additionalBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                assertBackendCompatibility(backendCase.name);
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testJavassistBackendCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            FastBeanCopierSupport.setBackend(new JavassistFastBeanCopierBackend());
            assertBackendCompatibility("javassist");
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testAccessorDirectTransferPreservesNullSkipSemantics() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            List<BackendCase> backends = Arrays.asList(
                new BackendCase("reflection-accessor", new ReflectionAccessorFastBeanCopierBackend()),
                new BackendCase("asm-accessor", new AsmAccessorFastBeanCopierBackend())
            );
            for (BackendCase backendCase : backends) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                Source source = new Source();
                source.setName(null);
                source.setAge(33);

                Target target = new Target();
                target.setName("keep-me");
                target.setAge(1);

                FastBeanCopierSupport.copy(source, target);

                Assert.assertEquals(backendCase.name, "keep-me", target.getName());
                Assert.assertEquals(backendCase.name, 33, target.getAge());
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testCrossClassLoaderCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : benchmarkBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                assertCrossClassLoaderCompatibility(backendCase.name);
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testDynamicClassLoaderUsesVolatileCacheAndReflectionFallback() throws Exception {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try (URLClassLoader loader = createTestClassLoader()) {
            FastBeanCopierSupport.setBackend(new AsmAccessorFastBeanCopierBackend());
            FastBeanCopierSupport.clearCache();

            Class<?> sourceClass = loader.loadClass(Source.class.getName());
            Class<?> targetClass = loader.loadClass(Target.class.getName());

            Assert.assertTrue(FastBeanCopierSupport.usesVolatileClassLoader(sourceClass, targetClass));
            Assert.assertTrue(FastBeanCopierSupport.getEffectiveBackend(sourceClass, targetClass)
                                  instanceof ReflectionAccessorFastBeanCopierBackend);

            Object source = sourceClass.getDeclaredConstructor().newInstance();
            FastBeanCopier.copy(createCrossClassLoaderSourceMap(), source);
            Object target = targetClass.getDeclaredConstructor().newInstance();

            Copier copier1 = FastBeanCopierSupport.getCopier(source, target, true);
            Copier copier2 = FastBeanCopierSupport.getCopier(source, target, true);

            Assert.assertSame(copier1, copier2);
            Assert.assertEquals(0, FastBeanCopierSupport.getStableCacheSize());
            Assert.assertEquals(2, FastBeanCopierSupport.getVolatileCacheSize());

            FastBeanCopier.copy(source, target);
            Map<String, Object> copied = FastBeanCopier.copy(target, new HashMap<>());
            Assert.assertEquals("cross-loader", copied.get("name"));
            Assert.assertEquals(24, copied.get("age"));
        } finally {
            FastBeanCopierSupport.setBackend(original);
            FastBeanCopierSupport.clearCache();
        }
    }

    @Test
    public void testClearCacheByClassLoaderRecreatesVolatileCopier() throws Exception {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try (URLClassLoader loader = createTestClassLoader()) {
            FastBeanCopierSupport.setBackend(new AsmAccessorFastBeanCopierBackend());
            FastBeanCopierSupport.clearCache();

            Class<?> sourceClass = loader.loadClass(Source.class.getName());
            Class<?> targetClass = loader.loadClass(Target.class.getName());
            Object source = sourceClass.getDeclaredConstructor().newInstance();
            Object target = targetClass.getDeclaredConstructor().newInstance();
            FastBeanCopier.copy(createCrossClassLoaderSourceMap(), source);

            Copier first = FastBeanCopierSupport.getCopier(source, target, true);
            Assert.assertEquals(0, FastBeanCopierSupport.getStableCacheSize());
            Assert.assertEquals(2, FastBeanCopierSupport.getVolatileCacheSize());

            FastBeanCopierSupport.clearCache(loader);

            Assert.assertEquals(0, FastBeanCopierSupport.getVolatileCacheSize());

            Copier second = FastBeanCopierSupport.getCopier(source, target, true);
            Assert.assertNotSame(first, second);
        } finally {
            FastBeanCopierSupport.setBackend(original);
            FastBeanCopierSupport.clearCache();
        }
    }

    @Test
    public void testClearCacheByClassLoader() throws Exception {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try (URLClassLoader loader = createTestClassLoader()) {
            FastBeanCopierSupport.setBackend(new ReflectionAccessorFastBeanCopierBackend());
            FastBeanCopierSupport.clearCache();

            Class<?> sourceClass = loader.loadClass(Source.class.getName());
            Class<?> targetClass = loader.loadClass(Target.class.getName());
            Object source = sourceClass.getDeclaredConstructor().newInstance();
            Object target = targetClass.getDeclaredConstructor().newInstance();
            FastBeanCopier.copy(createCrossClassLoaderSourceMap(), source);

            FastBeanCopierSupport.getCopier(source, target, true);
            FastBeanCopierSupport.DEFAULT_CONVERT.convert(Collections.singletonMap("name", "loader"),
                                                         targetClass,
                                                         FastBeanCopierSupport.EMPTY_CLASS_ARRAY);

            Assert.assertTrue(FastBeanCopierSupport.getVolatileCacheSize() > 0);

            FastBeanCopier.clearCache(loader);

            Assert.assertEquals(0, FastBeanCopierSupport.getVolatileCacheSize());

            Copier recreated = FastBeanCopierSupport.getCopier(source, target, true);
            Assert.assertNotNull(recreated);
        } finally {
            FastBeanCopierSupport.setBackend(original);
            FastBeanCopierSupport.clearCache();
        }
    }

    @Test
    public void testAdditionalBackendComplexConversionCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : benchmarkBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                Target fromBean = FastBeanCopierSupport.copy(createComplexBenchmarkSource(), new Target());
                assertTargetCopied(createComplexBenchmarkSource(), fromBean);

                Target fromMap = FastBeanCopierSupport.copy(createHeterogeneousBenchmarkMap(), new Target());
                Assert.assertEquals(backendCase.name, "heterogeneous-benchmark", fromMap.getName());
                Assert.assertEquals(backendCase.name, Boolean.TRUE, fromMap.getBoy());
                Assert.assertFalse(backendCase.name, fromMap.isBoy2());
                Assert.assertEquals(backendCase.name, "1", fromMap.getBoy3());
                Assert.assertEquals(backendCase.name, 123, fromMap.getAge());
                Assert.assertEquals(backendCase.name, 456, fromMap.getAge2());
                Assert.assertEquals(backendCase.name, Color.BLUE, fromMap.getColor3());
                Assert.assertArrayEquals(backendCase.name, new long[]{39L, 40L}, fromMap.getArr7());
                assertNumberListEquals(backendCase.name, Arrays.asList(41, 42), fromMap.getArr8());
                Assert.assertNotNull(backendCase.name, fromMap.getNestObject());
                Assert.assertEquals(backendCase.name, "map-nest-1", fromMap.getNestObject().getName());
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testAdditionalBackendConversionHeavyMapCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : benchmarkBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                Target target = FastBeanCopierSupport.copy(createConversionHeavyBenchmarkMap(), new Target());
                Assert.assertEquals(backendCase.name, "123456", target.getName());
                Assert.assertArrayEquals(backendCase.name, new String[]{"alpha", "beta", "gamma"}, target.getIds());
                Assert.assertEquals(backendCase.name, Boolean.TRUE, target.getBoy());
                Assert.assertTrue(backendCase.name, target.isBoy2());
                Assert.assertEquals(backendCase.name, "false", target.getBoy3());
                Assert.assertEquals(backendCase.name, 2048, target.getAge());
                Assert.assertEquals(backendCase.name, 4096, target.getAge2());
                Assert.assertEquals(backendCase.name, "8192", target.getAge3());
                Assert.assertEquals(backendCase.name, 1, target.getColor());
                Assert.assertEquals(backendCase.name, Color.RED, target.getColor2());
                Assert.assertEquals(backendCase.name, Color.BLUE, target.getColor3());
                Assert.assertArrayEquals(backendCase.name, new String[]{"51", "52"}, target.getArr());
                Assert.assertArrayEquals(backendCase.name, new long[]{59L, 60L}, target.getArr7());
                assertNumberListEquals(backendCase.name, Arrays.asList(61, 62), target.getArr8());
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testAdditionalBackendCollectionHeavyMapCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : benchmarkBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                Target target = FastBeanCopierSupport.copy(createCollectionHeavyBenchmarkMap(), new Target());
                Assert.assertEquals(backendCase.name, "collection-heavy", target.getName());
                Assert.assertArrayEquals(backendCase.name, new String[]{"alpha", "beta"}, target.getIds());
                Assert.assertArrayEquals(backendCase.name, new String[]{"1", "2"}, target.getArr());
                Assert.assertEquals(backendCase.name, Arrays.asList("3", "4"), target.getArr2());
                Assert.assertArrayEquals(backendCase.name, new Integer[]{5, 6}, target.getArr3());
                Assert.assertArrayEquals(backendCase.name, new Integer[]{7, 8}, target.getArr4());
                Assert.assertArrayEquals(backendCase.name, new long[]{9L, 10L}, target.getArr7());
                assertNumberListEquals(backendCase.name, Arrays.asList(11, 12), target.getArr8());
                Assert.assertArrayEquals(backendCase.name, new Color[]{Color.BLUE, Color.RED}, target.getColors());
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testAdditionalBackendNestedHeavyMapCompatibility() {
        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            for (BackendCase backendCase : benchmarkBackends()) {
                FastBeanCopierSupport.setBackend(backendCase.backend);
                Target target = FastBeanCopierSupport.copy(createNestedHeavyBenchmarkMap(), new Target());
                Assert.assertEquals(backendCase.name, "nested-heavy", target.getName());
                Assert.assertNotNull(backendCase.name, target.getNestObject());
                Assert.assertEquals(backendCase.name, "nest-root", target.getNestObject().getName());
                Assert.assertEquals(backendCase.name, 31, target.getNestObject().getAge());
                Assert.assertNotNull(backendCase.name, target.getNestObject2());
                Assert.assertEquals(backendCase.name, "nest-map", target.getNestObject2().getName());
                Assert.assertEquals(backendCase.name, 32, target.getNestObject2().getAge());
                Assert.assertNotNull(backendCase.name, target.getNestObject3());
                Assert.assertEquals(backendCase.name, "nest-tail", String.valueOf(target.getNestObject3().get("name")));
                Assert.assertNotNull(backendCase.name, target.getNestObjects());
                Assert.assertEquals(backendCase.name, 3, target.getNestObjects().size());
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testBackendPerformanceComparison() {
        Source source = createBenchmarkSource();
        int warmup = 5_000;
        int iterations = 50_000;

        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            List<BackendCase> backends = benchmarkBackends();
            Map<String, BenchmarkResult> round1 = benchmarkBackends(backends, source, warmup, iterations);
            List<BackendCase> reversed = Arrays.asList(
                backends.get(3),
                backends.get(2),
                backends.get(1),
                backends.get(0)
            );
            Map<String, BenchmarkResult> round2 = benchmarkBackends(reversed, source, warmup, iterations);
            Map<String, BenchmarkResult> average = average(round1, round2);
            BenchmarkResult javassist = average.get("javassist");

            System.out.println("\n=== FastBeanCopier backend 性能对比 ===");
            System.out.println("预热次数: " + warmup + ", 正式迭代: " + iterations);
            System.out.println("-- round1");
            round1.values().forEach(this::printBenchmark);
            System.out.println("-- round2");
            round2.values().forEach(this::printBenchmark);
            System.out.println("-- average");
            average.values().forEach(this::printBenchmark);
            for (BenchmarkResult result : average.values()) {
                if ("javassist".equals(result.name)) {
                    continue;
                }
                System.out.println("cached ratio  (" + result.name + "/javassist): " + formatRatio(result.cachedWarmNanos, javassist.cachedWarmNanos));
                System.out.println("support ratio (" + result.name + "/javassist): " + formatRatio(result.supportWarmNanos, javassist.supportWarmNanos));
                System.out.println("cold create ratio (" + result.name + "/javassist): " + formatRatio(result.backendColdNanos, javassist.backendColdNanos));
                System.out.println("cold first copy ratio (" + result.name + "/javassist): " + formatRatio(result.supportColdNanos, javassist.supportColdNanos));
            }

            for (BenchmarkResult result : average.values()) {
                Assert.assertTrue(result.cachedWarmNanos > 0);
                Assert.assertTrue(result.supportWarmNanos > 0);
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testBackendPerformanceAcrossScenarios() {
        int warmup = 3_000;
        int iterations = 20_000;

        FastBeanCopierBackend original = FastBeanCopierSupport.getBackend();
        try {
            System.out.println("\n=== FastBeanCopier 多场景性能对比 ===");
            System.out.println("预热次数: " + warmup + ", 正式迭代: " + iterations);
            for (BenchmarkScenario scenario : benchmarkScenarios()) {
                System.out.println("-- scenario: " + scenario.name);
                Map<String, ScenarioBenchmarkResult> results = benchmarkScenarioBackends(scenario, warmup, iterations);
                results.values().forEach(this::printScenarioBenchmark);
                BenchmarkResult baseline = results.get("javassist");
                for (ScenarioBenchmarkResult result : results.values()) {
                    if ("javassist".equals(result.name)) {
                        continue;
                    }
                    System.out.println("cached ratio  (" + result.name + "/javassist): " + formatRatio(result.cachedWarmNanos, baseline.cachedWarmNanos));
                    System.out.println("support ratio (" + result.name + "/javassist): " + formatRatio(result.supportWarmNanos, baseline.supportWarmNanos));
                }
            }
        } finally {
            FastBeanCopierSupport.setBackend(original);
        }
    }

    @Test
    public void testRecordCopyPerformance() {
        Map<String, Object> mapSource = createRecordBenchmarkMap();
        RecordSource recordSource = new RecordSource("record-source",
                                                     28,
                                                     Color.BLUE,
                                                     new FastBeanCopierTest.NestedRecord("nested-source"),
                                                     Arrays.asList(new FastBeanCopierTest.NestedRecord("nested-list")));
        int warmup = 3_000;
        int iterations = 20_000;

        warmupRecordCopy(mapSource, recordSource, warmup);
        long mapToRecord = runRecordCopyBenchmark(mapSource, iterations);
        long asmRecordToRecord = runRecordCopyBenchmark(recordSource, iterations);

        long generatedRecordToRecord = runWithRecordAsmDisabled(() -> {
            warmupRecordCopy(mapSource, recordSource, warmup);
            return runRecordCopyBenchmark(recordSource, iterations);
        });
        FastBeanCopierTest.TargetRecord target = FastBeanCopier.copy(mapSource, FastBeanCopierTest.TargetRecord.class);
        assertRecordCopied(target);

        System.out.println("\n=== FastBeanCopier record 性能 ===");
        System.out.println("预热次数: " + warmup + ", 正式迭代: " + iterations);
        System.out.println(String.format(Locale.ROOT,
                                         "map->record    total=%8.2fms avg=%8.2fns/op",
                                         mapToRecord / 1_000_000.0,
                                         (double) mapToRecord / iterations));
        System.out.println(String.format(Locale.ROOT,
                                         "record->record asm       total=%8.2fms avg=%8.2fns/op",
                                         asmRecordToRecord / 1_000_000.0,
                                         (double) asmRecordToRecord / iterations));
        System.out.println(String.format(Locale.ROOT,
                                         "record->record generated total=%8.2fms avg=%8.2fns/op",
                                         generatedRecordToRecord / 1_000_000.0,
                                         (double) generatedRecordToRecord / iterations));
        System.out.println("record asm/generated ratio: " + formatRatio(asmRecordToRecord, generatedRecordToRecord));
        Assert.assertTrue(mapToRecord > 0);
        Assert.assertTrue(asmRecordToRecord > 0);
        Assert.assertTrue(generatedRecordToRecord > 0);
    }

    private void warmupRecordCopy(Map<String, Object> mapSource, RecordSource recordSource, int warmup) {
        FastBeanCopierSupport.clearCache();
        for (int i = 0; i < warmup; i++) {
            assertRecordCopied(FastBeanCopier.copy(mapSource, FastBeanCopierTest.TargetRecord.class));
            FastBeanCopierTest.TargetRecord recordTarget =
                FastBeanCopier.copy(recordSource, FastBeanCopierTest.TargetRecord.class);
            Assert.assertEquals("record-source", recordTarget.name());
        }
    }

    private long runWithRecordAsmDisabled(Supplier<Long> action) {
        String previous = System.getProperty(RecordBeanCopierSupport.ASM_DISABLED_PROPERTY);
        System.setProperty(RecordBeanCopierSupport.ASM_DISABLED_PROPERTY, "true");
        FastBeanCopierSupport.clearCache();
        try {
            return action.get();
        } finally {
            if (previous == null) {
                System.clearProperty(RecordBeanCopierSupport.ASM_DISABLED_PROPERTY);
            } else {
                System.setProperty(RecordBeanCopierSupport.ASM_DISABLED_PROPERTY, previous);
            }
            FastBeanCopierSupport.clearCache();
        }
    }

    private long runCachedCopierBenchmark(Source source, Copier copier, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            copier.copy(source, new Target(), Collections.emptySet(), FastBeanCopierSupport.DEFAULT_CONVERT);
        }
        return System.nanoTime() - start;
    }

    private long runSupportBenchmark(Source source, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FastBeanCopierSupport.copy(source, new Target());
        }
        return System.nanoTime() - start;
    }

    private long runCachedCopierBenchmark(Object source, Supplier<Object> targetSupplier, Copier copier, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            copier.copy(source, targetSupplier.get(), Collections.emptySet(), FastBeanCopierSupport.DEFAULT_CONVERT);
        }
        return System.nanoTime() - start;
    }

    private long runSupportBenchmark(Object source, Supplier<Object> targetSupplier, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FastBeanCopierSupport.copy(source, targetSupplier.get());
        }
        return System.nanoTime() - start;
    }

    private long runRecordCopyBenchmark(Object source, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FastBeanCopier.copy(source, FastBeanCopierTest.TargetRecord.class);
        }
        return System.nanoTime() - start;
    }

    private Map<String, Object> createRecordBenchmarkMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "record-target");
        map.put("age", "18");
        map.put("color2", "RED");
        map.put("nested", Collections.singletonMap("name", "nested-map"));
        map.put("nestedList", Collections.singletonList(Collections.singletonMap("name", "nested-list")));
        return map;
    }

    private void assertRecordCopied(FastBeanCopierTest.TargetRecord target) {
        Assert.assertEquals("record-target", target.name());
        Assert.assertEquals(18, target.age());
        Assert.assertEquals(Color.RED, target.color2());
        Assert.assertNotNull(target.nested());
        Assert.assertEquals("nested-map", target.nested().name());
        Assert.assertNotNull(target.nestedList());
        Assert.assertEquals(1, target.nestedList().size());
        Assert.assertEquals("nested-list", target.nestedList().get(0).name());
    }

    private long runBackendColdBenchmark(Source source, FastBeanCopierBackend backend) {
        long start = System.nanoTime();
        Copier copier = backend.createCopier(Source.class, Target.class);
        copier.copy(source, new Target(), Collections.emptySet(), FastBeanCopierSupport.DEFAULT_CONVERT);
        return System.nanoTime() - start;
    }

    private long runSupportColdBenchmark(Source source) {
        FastBeanCopierSupport.clearCache();
        long start = System.nanoTime();
        FastBeanCopierSupport.copy(source, new Target());
        return System.nanoTime() - start;
    }

    private BenchmarkResult benchmarkBackend(String name,
                                             FastBeanCopierBackend backend,
                                             Source source,
                                             int warmup,
                                             int iterations) {
        FastBeanCopierSupport.setBackend(backend);
        FastBeanCopierSupport.clearCache();
        Copier cachedCopier = FastBeanCopierSupport.getCopier(source, new Target(), true);
        for (int i = 0; i < warmup; i++) {
            cachedCopier.copy(source, new Target(), Collections.emptySet(), FastBeanCopierSupport.DEFAULT_CONVERT);
            FastBeanCopierSupport.copy(source, new Target());
        }
        return new BenchmarkResult(name,
                                   runCachedCopierBenchmark(source, cachedCopier, iterations),
                                   runSupportBenchmark(source, iterations),
                                   runBackendColdBenchmark(source, backend),
                                   runSupportColdBenchmark(source));
    }

    private Map<String, BenchmarkResult> benchmarkBackends(List<BackendCase> backends,
                                                           Source source,
                                                           int warmup,
                                                           int iterations) {
        Map<String, BenchmarkResult> results = new LinkedHashMap<>();
        for (BackendCase backend : backends) {
            results.put(backend.name, benchmarkBackend(backend.name, backend.backend, source, warmup, iterations));
        }
        return results;
    }

    private Map<String, ScenarioBenchmarkResult> benchmarkScenarioBackends(BenchmarkScenario scenario,
                                                                           int warmup,
                                                                           int iterations) {
        Map<String, ScenarioBenchmarkResult> results = new LinkedHashMap<>();
        for (BackendCase backend : benchmarkBackends()) {
            FastBeanCopierSupport.setBackend(backend.backend);
            FastBeanCopierSupport.clearCache();
            Object source = scenario.sourceSupplier.get();
            Object warmupTarget = scenario.targetSupplier.get();
            Copier copier = FastBeanCopierSupport.getCopier(source, warmupTarget, true);
            for (int i = 0; i < warmup; i++) {
                Object cachedResult = scenario.targetSupplier.get();
                copier.copy(source,
                            cachedResult,
                            Collections.emptySet(),
                            FastBeanCopierSupport.DEFAULT_CONVERT);
                scenario.resultChecker.accept(cachedResult);
                Object supportResult = FastBeanCopierSupport.copy(source, scenario.targetSupplier.get());
                scenario.resultChecker.accept(supportResult);
            }
            long cached = runCachedCopierBenchmark(source, scenario.targetSupplier, copier, iterations);
            long support = runSupportBenchmark(source, scenario.targetSupplier, iterations);
            results.put(backend.name, new ScenarioBenchmarkResult(scenario.name, backend.name, cached, support));
        }
        return results;
    }

    private Map<String, BenchmarkResult> average(Map<String, BenchmarkResult> first,
                                                 Map<String, BenchmarkResult> second) {
        Map<String, BenchmarkResult> average = new LinkedHashMap<>();
        for (Map.Entry<String, BenchmarkResult> entry : first.entrySet()) {
            average.put(entry.getKey(), BenchmarkResult.average(entry.getKey(), entry.getValue(), second.get(entry.getKey())));
        }
        return average;
    }

    private void printBenchmark(BenchmarkResult result) {
        System.out.println(String.format(Locale.ROOT,
                                         "%-18s cached=%8.2fms support=%8.2fms coldCreate=%8.3fms coldFirstCopy=%8.3fms",
                                         result.name,
                                         result.cachedWarmNanos / 1_000_000.0,
                                         result.supportWarmNanos / 1_000_000.0,
                                         result.backendColdNanos / 1_000_000.0,
                                         result.supportColdNanos / 1_000_000.0));
    }

    private void printScenarioBenchmark(ScenarioBenchmarkResult result) {
        System.out.println(String.format(Locale.ROOT,
                                         "%-18s cached=%8.2fms support=%8.2fms",
                                         result.name,
                                         result.cachedWarmNanos / 1_000_000.0,
                                         result.supportWarmNanos / 1_000_000.0));
    }

    private String formatRatio(long value, long base) {
        return String.format(Locale.ROOT, "%.2fx", (double) value / (double) base);
    }

    private void assertBackendCompatibility(String backendName) {
        Source source = createBenchmarkSource();
        source.setIds(new String[]{"1", "2"});
        source.setArr2(new String[]{"1", "2"});
        source.setArr7(new int[]{1, 2});
        source.setColors(new Color[]{Color.BLUE, Color.RED});

        Target target = FastBeanCopier.copy(source, new Target());
        assertTargetCopied(source, target);
        Assert.assertArrayEquals(backendName, source.getIds(), target.getIds());
        Assert.assertNotSame(backendName, source.getIds(), target.getIds());
        Assert.assertArrayEquals(backendName, source.getColors(), target.getColors());
        Assert.assertNotSame(backendName, source.getColors(), target.getColors());

        Map<String, Object> map = FastBeanCopier.copy(source, new HashMap<>());
        Assert.assertEquals(backendName, source.getName(), map.get("name"));
        Assert.assertEquals(backendName, source.getAge(), map.get("age"));

        FastBeanCopierTest.ExtendableEntity extendable = FastBeanCopier.copy(source, new FastBeanCopierTest.ExtendableEntity());
        Assert.assertEquals(backendName, source.getName(), extendable.getName());
        Assert.assertEquals(backendName, source.getAge(), extendable.getExtension("age"));
        Assert.assertEquals(backendName, source.getColor(), extendable.getExtension("color"));
    }

    private void assertCrossClassLoaderCompatibility(String backendName) {
        FastBeanCopierSupport.clearCache();
        try (URLClassLoader loader = createTestClassLoader()) {
            Class<?> sourceClass = loader.loadClass(Source.class.getName());
            Class<?> targetClass = loader.loadClass(Target.class.getName());

            Assert.assertNotSame(backendName, sourceClass, Source.class);
            Assert.assertNotSame(backendName, targetClass, Target.class);

            Object source = sourceClass.getDeclaredConstructor().newInstance();
            FastBeanCopier.copy(createCrossClassLoaderSourceMap(), source);

            Map<String, Object> copiedFromSource = FastBeanCopier.copy(source, new HashMap<>());
            Assert.assertEquals(backendName, "cross-loader", copiedFromSource.get("name"));
            Assert.assertEquals(backendName, 24, copiedFromSource.get("age"));
            Assert.assertEquals(backendName, "RED", String.valueOf(copiedFromSource.get("color")));

            Object target = targetClass.getDeclaredConstructor().newInstance();
            FastBeanCopier.copy(source, target);

            Map<String, Object> copiedFromTarget = FastBeanCopier.copy(target, new HashMap<>());
            Assert.assertEquals(backendName, "cross-loader", copiedFromTarget.get("name"));
            Assert.assertEquals(backendName, 24, copiedFromTarget.get("age"));
            Assert.assertEquals(backendName, "RED", String.valueOf(copiedFromTarget.get("color2")));
            Assert.assertEquals(backendName, "BLUE", String.valueOf(copiedFromTarget.get("color3")));

            loader.close();
            Map<String, Object> copiedAfterClose = FastBeanCopier.copy(source, new HashMap<>());
            Assert.assertEquals(backendName, "cross-loader", copiedAfterClose.get("name"));
            Assert.assertEquals(backendName, 24, copiedAfterClose.get("age"));
        } catch (Exception e) {
            throw new AssertionError("Cross classloader compatibility failed for backend: " + backendName, e);
        } finally {
            FastBeanCopierSupport.clearCache();
        }
    }

    private Map<String, Object> createCrossClassLoaderSourceMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "cross-loader");
        values.put("age", 24);
        values.put("age2", 7);
        values.put("color", "RED");
        values.put("color2", "红色");
        values.put("color3", Color.BLUE.getValue());
        values.put("arr7", new int[]{1, 2});
        values.put("arr8", new int[]{1, 2});
        return values;
    }

    private URLClassLoader createTestClassLoader() throws IOException {
        URL classes = new File("target/test-classes").getAbsoluteFile().toURI().toURL();
        return new URLClassLoader(new URL[]{classes}, ClassUtils.getDefaultClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                try {
                    Class<?> type = loadSelfClass(name);
                    if (type != null) {
                        if (resolve) {
                            resolveClass(type);
                        }
                        return type;
                    }
                } catch (Throwable ignore) {
                    // ignore
                }
                return super.loadClass(name, resolve);
            }

            synchronized Class<?> loadSelfClass(String name) throws ClassNotFoundException {
                Class<?> type = findLoadedClass(name);
                if (type == null) {
                    type = findClass(name);
                    resolveClass(type);
                }
                return type;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return findResources(name);
            }

            @Override
            public URL getResource(String name) {
                return findResource(name);
            }
        };
    }

    private List<BackendCase> additionalBackends() {
        return Arrays.asList(
            new BackendCase("reflect", new ReflectFastBeanCopierBackend()),
            new BackendCase("reflection-accessor", new ReflectionAccessorFastBeanCopierBackend()),
            new BackendCase("asm-accessor", new AsmAccessorFastBeanCopierBackend())
        );
    }

    private List<BackendCase> benchmarkBackends() {
        return Arrays.asList(
            new BackendCase("javassist", new JavassistFastBeanCopierBackend()),
            new BackendCase("reflect", new ReflectFastBeanCopierBackend()),
            new BackendCase("reflection-accessor", new ReflectionAccessorFastBeanCopierBackend()),
            new BackendCase("asm-accessor", new AsmAccessorFastBeanCopierBackend())
        );
    }

    private void assertTargetCopied(Source source, Target target) {
        Assert.assertEquals(source.getName(), target.getName());
        Assert.assertEquals(source.getAge(), target.getAge());
        Assert.assertEquals(source.getAge2().intValue(), target.getAge2());
        Assert.assertEquals(source.getColor().getValue().intValue(), target.getColor());
        Assert.assertEquals(source.getColor(), target.getColor2());
        Assert.assertEquals(Color.BLUE, target.getColor3());
        Assert.assertArrayEquals(Arrays.stream(source.getArr7()).asLongStream().toArray(), target.getArr7());
        assertNumberListEquals("source", Arrays.stream(source.getArr8()).boxed().toList(), target.getArr8());
    }

    private void assertNumberListEquals(String message, List<Integer> expected, List<?> actual) {
        Assert.assertNotNull(message, actual);
        Assert.assertEquals(message, expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Object actualValue = actual.get(i);
            int normalized = actualValue instanceof Number
                ? ((Number) actualValue).intValue()
                : Integer.parseInt(String.valueOf(actualValue));
            Assert.assertEquals(message + "[" + i + "]",
                                expected.get(i).intValue(),
                                normalized);
        }
    }

    private Source createBenchmarkSource() {
        Source source = new Source();
        source.setName("benchmark");
        source.setAge(18);
        source.setAge2(20);
        source.setColor(Color.RED);
        source.setColor2("红色");
        source.setColor3(Color.BLUE.getValue());
        source.setArr7(new int[]{1, 2});
        source.setArr8(new int[]{1, 2});
        return source;
    }

    private Source createComplexBenchmarkSource() {
        Source source = new Source();
        source.setName("complex-benchmark");
        source.setIds(new String[]{"11", "12", "13"});
        source.setBoy(true);
        source.setBoy2(false);
        source.setBoy3(true);
        source.setAge(36);
        source.setAge2(40);
        source.setAge3(41);
        source.setColor(Color.RED);
        source.setColor2("RED");
        source.setColor3(Color.BLUE.getValue());
        source.setNestObject(new NestObject("nest-complex", 18, "1234567"));
        source.setNestObject2(new LinkedHashMap<>());
        source.getNestObject2().put("name", "nest-map");
        source.getNestObject2().put("age", 19);
        source.setNestObject3(new NestObject("nest-tail", 20, "7654321"));
        source.setNestObjects(Arrays.asList(
            new NestObject("nest-list-1", 21, "1234567"),
            new NestObject("nest-list-2", 22, "7654321")
        ));
        source.setArr(Arrays.asList("1", "2", "3"));
        source.setArr2(new String[]{"4", "5", "6"});
        source.setArr3(new String[]{"7", "8", "9"});
        source.setArr4(Arrays.asList("10", "11", "12"));
        source.setArr5(new String[]{"13", "14"});
        source.setArr6(new String[]{"15", "16"});
        source.setArr7(new int[]{17, 18});
        source.setArr8(new int[]{19, 20});
        source.setColors(new Color[]{Color.BLUE, Color.RED});
        return source;
    }

    private Map<String, Object> createHeterogeneousBenchmarkMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "heterogeneous-benchmark");
        map.put("ids", Arrays.asList("101", "102", "103"));
        map.put("boy", "true");
        map.put("boy2", Boolean.FALSE);
        map.put("boy3", 1);
        map.put("age", "123");
        map.put("age2", 456L);
        map.put("age3", 789);
        map.put("deleteTime", "2024-05-07 10:20:30");
        map.put("createTime", "2024-05-08 11:30:40");
        map.put("updateTime", "2024-05-09 12:40:50");
        map.put("nestObject", new LinkedHashMap<String, Object>() {{
            put("name", "map-nest-1");
            put("age", "18");
            put("password", "1234567");
        }});
        map.put("nestObject2", new LinkedHashMap<String, Object>() {{
            put("name", "map-nest-2");
            put("age", 19);
        }});
        map.put("nestObject3", new LinkedHashMap<String, Object>() {{
            put("name", "map-nest-3");
            put("age", 20);
            put("password", "7654321");
        }});
        map.put("nestObjects", Arrays.asList(
            new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-1");
                put("age", "21");
                put("password", "1234567");
            }},
            new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-2");
                put("age", 22);
                put("password", "7654321");
            }}
        ));
        map.put("color", Color.RED);
        map.put("color2", "RED");
        map.put("color3", Color.BLUE.getValue());
        map.put("arr", Arrays.asList("31", "32"));
        map.put("arr2", Arrays.asList("33", "34"));
        map.put("arr3", new String[]{"35", "36"});
        map.put("arr4", Arrays.asList("37", "38"));
        map.put("arr7", new int[]{39, 40});
        map.put("arr8", Arrays.asList("41", "42"));
        map.put("colors", Arrays.asList("BLUE", "RED"));
        return map;
    }

    private Map<String, Object> createConversionHeavyBenchmarkMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", 123456);
        map.put("ids", "alpha,beta,gamma");
        map.put("boy", "true");
        map.put("boy2", 1);
        map.put("boy3", false);
        map.put("age", "2048");
        map.put("age2", 4096L);
        map.put("age3", 8192);
        map.put("deleteTime", "2024-05-07 10:20:30");
        map.put("createTime", new Date(1715043600000L));
        map.put("updateTime", "2024-05-08 11:30:40");
        map.put("nestObject", new LinkedHashMap<String, Object>() {{
            put("name", 1001);
            put("age", "28");
            put("password", "1234567");
        }});
        map.put("nestObject2", new LinkedHashMap<String, Object>() {{
            put("name", "map-nest-2");
            put("age", "29");
        }});
        map.put("nestObject3", new LinkedHashMap<String, Object>() {{
            put("name", "map-nest-3");
            put("age", 30);
            put("password", "7654321");
        }});
        map.put("nestObjects", Arrays.asList(
            new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-1");
                put("age", "31");
                put("password", "1234567");
            }},
            new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-2");
                put("age", 32);
                put("password", "7654321");
            }}
        ));
        map.put("color", Color.RED);
        map.put("color2", Color.RED.getText());
        map.put("color3", "BLUE");
        map.put("arr", "51,52");
        map.put("arr2", new String[]{"53", "54"});
        map.put("arr3", Arrays.asList("55", "56"));
        map.put("arr4", new String[]{"57", "58"});
        map.put("arr7", Arrays.asList("59", "60"));
        map.put("arr8", new int[]{61, 62});
        map.put("colors", new String[]{"BLUE", "RED"});
        return map;
    }

    private Map<String, Object> createCollectionHeavyBenchmarkMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "collection-heavy");
        map.put("ids", Arrays.asList("alpha", "beta"));
        map.put("arr", new String[]{"1", "2"});
        map.put("arr2", Arrays.asList("3", "4"));
        map.put("arr3", Arrays.asList(5, 6));
        map.put("arr4", Arrays.asList(7, 8));
        map.put("arr7", new long[]{9L, 10L});
        map.put("arr8", Arrays.asList(11, 12));
        map.put("colors", new Color[]{Color.BLUE, Color.RED});
        return map;
    }

    private Map<String, Object> createNestedHeavyBenchmarkMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "nested-heavy");
        map.put("nestObject", new LinkedHashMap<String, Object>() {{
            put("name", "nest-root");
            put("age", "31");
            put("password", "1234567");
        }});
        map.put("nestObject2", new LinkedHashMap<String, Object>() {{
            put("name", "nest-map");
            put("age", 32);
        }});
        map.put("nestObject3", new LinkedHashMap<String, Object>() {{
            put("name", "nest-tail");
            put("age", "33");
            put("password", "7654321");
        }});
        map.put("nestObjects", Arrays.asList(
            new LinkedHashMap<String, Object>() {{
                put("name", "nest-list-1");
                put("age", "34");
                put("password", "1234567");
            }},
            new LinkedHashMap<String, Object>() {{
                put("name", "nest-list-2");
                put("age", 35);
                put("password", "7654321");
            }},
            new LinkedHashMap<String, Object>() {{
                put("name", "nest-list-3");
                put("age", "36");
                put("password", "1357246");
            }}
        ));
        map.put("color2", "红色");
        map.put("color3", "BLUE");
        return map;
    }

    private List<BenchmarkScenario> benchmarkScenarios() {
        return Arrays.asList(
            new BenchmarkScenario("simple-bean->bean",
                                  () -> {
                                      FastBeanCopierJmhBenchmark.SimpleSource source = new FastBeanCopierJmhBenchmark.SimpleSource();
                                      source.setName("simple-benchmark");
                                      source.setAge(18);
                                      source.setEnabled(true);
                                      source.setScore(88L);
                                      source.setCreated(new java.util.Date(1715040000000L));
                                      return source;
                                  },
                                  FastBeanCopierJmhBenchmark.SimpleTarget::new,
                                  result -> Assert.assertNotNull(((FastBeanCopierJmhBenchmark.SimpleTarget) result).getName())),
            new BenchmarkScenario("complex-bean->bean",
                                  this::createComplexBenchmarkSource,
                                  Target::new,
                                  result -> assertTargetCopied(createComplexBenchmarkSource(), (Target) result)),
            new BenchmarkScenario("heterogeneous-map->bean",
                                  this::createHeterogeneousBenchmarkMap,
                                  Target::new,
                                  result -> {
                                      Target target = (Target) result;
                                      Assert.assertEquals("heterogeneous-benchmark", target.getName());
                                      Assert.assertEquals(Boolean.TRUE, target.getBoy());
                                      Assert.assertFalse(target.isBoy2());
                                      Assert.assertEquals("1", target.getBoy3());
                                      Assert.assertEquals(123, target.getAge());
                                      Assert.assertEquals(456, target.getAge2());
                                      Assert.assertEquals(Color.BLUE, target.getColor3());
                                      Assert.assertArrayEquals(new long[]{39L, 40L}, target.getArr7());
                                      assertNumberListEquals("heterogeneous-map->bean", Arrays.asList(41, 42), target.getArr8());
                                  }),
            new BenchmarkScenario("conversion-heavy-map->bean",
                                  this::createConversionHeavyBenchmarkMap,
                                  Target::new,
                                  result -> {
                                      Target target = (Target) result;
                                      Assert.assertEquals("123456", target.getName());
                                      Assert.assertArrayEquals(new String[]{"alpha", "beta", "gamma"}, target.getIds());
                                      Assert.assertEquals(Boolean.TRUE, target.getBoy());
                                      Assert.assertTrue(target.isBoy2());
                                      Assert.assertEquals("false", target.getBoy3());
                                      Assert.assertEquals(2048, target.getAge());
                                      Assert.assertEquals(4096, target.getAge2());
                                      Assert.assertEquals("8192", target.getAge3());
                                      Assert.assertEquals(1, target.getColor());
                                      Assert.assertEquals(Color.RED, target.getColor2());
                                      Assert.assertEquals(Color.BLUE, target.getColor3());
                                      Assert.assertArrayEquals(new String[]{"51", "52"}, target.getArr());
                                      Assert.assertArrayEquals(new long[]{59L, 60L}, target.getArr7());
                                      assertNumberListEquals("conversion-heavy-map->bean", Arrays.asList(61, 62), target.getArr8());
                                  }),
            new BenchmarkScenario("collection-heavy-map->bean",
                                  this::createCollectionHeavyBenchmarkMap,
                                  Target::new,
                                  result -> {
                                      Target target = (Target) result;
                                      Assert.assertEquals("collection-heavy", target.getName());
                                      Assert.assertArrayEquals(new String[]{"alpha", "beta"}, target.getIds());
                                      Assert.assertArrayEquals(new String[]{"1", "2"}, target.getArr());
                                      Assert.assertEquals(Arrays.asList("3", "4"), target.getArr2());
                                      Assert.assertArrayEquals(new Integer[]{5, 6}, target.getArr3());
                                      Assert.assertArrayEquals(new Integer[]{7, 8}, target.getArr4());
                                      Assert.assertArrayEquals(new long[]{9L, 10L}, target.getArr7());
                                      assertNumberListEquals("collection-heavy-map->bean", Arrays.asList(11, 12), target.getArr8());
                                      Assert.assertArrayEquals(new Color[]{Color.BLUE, Color.RED}, target.getColors());
                                  }),
            new BenchmarkScenario("nested-heavy-map->bean",
                                  this::createNestedHeavyBenchmarkMap,
                                  Target::new,
                                  result -> {
                                      Target target = (Target) result;
                                      Assert.assertEquals("nested-heavy", target.getName());
                                      Assert.assertNotNull(target.getNestObject());
                                      Assert.assertEquals("nest-root", target.getNestObject().getName());
                                      Assert.assertEquals(31, target.getNestObject().getAge());
                                      Assert.assertNotNull(target.getNestObject2());
                                      Assert.assertEquals("nest-map", target.getNestObject2().getName());
                                      Assert.assertEquals(32, target.getNestObject2().getAge());
                                      Assert.assertNotNull(target.getNestObject3());
                                      Assert.assertEquals("nest-tail", String.valueOf(target.getNestObject3().get("name")));
                                      Assert.assertNotNull(target.getNestObjects());
                                      Assert.assertEquals(3, target.getNestObjects().size());
                                  }),
            new BenchmarkScenario("complex-bean->map",
                                  this::createComplexBenchmarkSource,
                                  HashMap::new,
                                  result -> {
                                      Map<?, ?> map = (Map<?, ?>) result;
                                      Assert.assertEquals("complex-benchmark", map.get("name"));
                                      Assert.assertEquals(36, map.get("age"));
                                      Assert.assertEquals("RED", String.valueOf(map.get("color2")));
                                  })
        );
    }

    static class BenchmarkResult {
        final String name;
        final long cachedWarmNanos;
        final long supportWarmNanos;
        final long backendColdNanos;
        final long supportColdNanos;

        BenchmarkResult(String name,
                        long cachedWarmNanos,
                        long supportWarmNanos,
                        long backendColdNanos,
                        long supportColdNanos) {
            this.name = name;
            this.cachedWarmNanos = cachedWarmNanos;
            this.supportWarmNanos = supportWarmNanos;
            this.backendColdNanos = backendColdNanos;
            this.supportColdNanos = supportColdNanos;
        }

        static BenchmarkResult average(String name, BenchmarkResult first, BenchmarkResult second) {
            return new BenchmarkResult(name,
                                       (first.cachedWarmNanos + second.cachedWarmNanos) / 2,
                                       (first.supportWarmNanos + second.supportWarmNanos) / 2,
                                       (first.backendColdNanos + second.backendColdNanos) / 2,
                                       (first.supportColdNanos + second.supportColdNanos) / 2);
        }
    }

    static class ScenarioBenchmarkResult extends BenchmarkResult {
        private final String scenario;

        ScenarioBenchmarkResult(String scenario, String name, long cachedWarmNanos, long supportWarmNanos) {
            super(name, cachedWarmNanos, supportWarmNanos, 0, 0);
            this.scenario = scenario;
        }
    }

    static class BackendCase {
        private final String name;
        private final FastBeanCopierBackend backend;

        BackendCase(String name, FastBeanCopierBackend backend) {
            this.name = name;
            this.backend = backend;
        }
    }

    static class BenchmarkScenario {
        private final String name;
        private final Supplier<Object> sourceSupplier;
        private final Supplier<Object> targetSupplier;
        private final Consumer<Object> resultChecker;

        BenchmarkScenario(String name,
                          Supplier<Object> sourceSupplier,
                          Supplier<Object> targetSupplier,
                          Consumer<Object> resultChecker) {
            this.name = name;
            this.sourceSupplier = sourceSupplier;
            this.targetSupplier = targetSupplier;
            this.resultChecker = resultChecker;
        }
    }

    public record RecordSource(String name,
                               int age,
                               Color color2,
                               FastBeanCopierTest.NestedRecord nested,
                               List<FastBeanCopierTest.NestedRecord> nestedList) {
    }
}
