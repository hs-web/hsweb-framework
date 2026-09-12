package org.hswebframework.web.bean;

import org.hswebframework.ezorm.core.DefaultExtendable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Threads(1)
@Fork(1)
public class FastBeanCopierJmhBenchmark {

    @Benchmark
    public void copySimpleBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.simpleSource, new SimpleTarget()));
    }

    @Benchmark
    public void copyComplexBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.complexSource, new Target()));
    }

    @Benchmark
    public void copyBeanToMap(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.complexSource, new HashMap<>()));
    }

    @Benchmark
    public void copyHeterogeneousMapToBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.heterogeneousMap, new Target()));
    }

    @Benchmark
    public void copyConversionHeavyMapToBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.conversionHeavyMap, new Target()));
    }

    @Benchmark
    public void copyCollectionHeavyMapToBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.collectionHeavyMap, new Target()));
    }

    @Benchmark
    public void copyNestedHeavyMapToBean(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.nestedHeavyMap, new Target()));
    }

    @Benchmark
    public void copyNestedHeavyMapToBeanOnly(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.nestedOnlyMap, new NestedOnlyTarget()));
    }

    @Benchmark
    public void copyBeanToExtendable(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.complexSource, new BenchmarkExtendableEntity()));
    }

    @Benchmark
    public void copyExtendableToMap(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(FastBeanCopierSupport.copy(state.extendableSource, new HashMap<>()));
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({"javassist", "reflect", "reflection-accessor", "asm-accessor"})
        public String backend;

        private FastBeanCopierBackend originalBackend;
        private SimpleSource simpleSource;
        private Source complexSource;
        private Map<String, Object> heterogeneousMap;
        private Map<String, Object> conversionHeavyMap;
        private Map<String, Object> collectionHeavyMap;
        private Map<String, Object> nestedHeavyMap;
        private Map<String, Object> nestedOnlyMap;
        private BenchmarkExtendableEntity extendableSource;

        @Setup(Level.Trial)
        public void setup() {
            originalBackend = FastBeanCopierSupport.getBackend();
            FastBeanCopierSupport.setBackend(createBackend(backend));
            FastBeanCopierSupport.clearCache();

            simpleSource = createSimpleSource();
            complexSource = createComplexSource();
            heterogeneousMap = createHeterogeneousMap();
            conversionHeavyMap = createConversionHeavyMap();
            collectionHeavyMap = createCollectionHeavyMap();
            nestedHeavyMap = createNestedHeavyMap();
            nestedOnlyMap = createNestedOnlyMap();
            extendableSource = FastBeanCopierSupport.copy(complexSource, new BenchmarkExtendableEntity());

            warmupCache();
        }

        @TearDown(Level.Trial)
        public void teardown() {
            FastBeanCopierSupport.setBackend(originalBackend);
        }

        private void warmupCache() {
            FastBeanCopierSupport.copy(simpleSource, new SimpleTarget());
            FastBeanCopierSupport.copy(complexSource, new Target());
            FastBeanCopierSupport.copy(complexSource, new HashMap<>());
            FastBeanCopierSupport.copy(heterogeneousMap, new Target());
            FastBeanCopierSupport.copy(conversionHeavyMap, new Target());
            FastBeanCopierSupport.copy(collectionHeavyMap, new Target());
            FastBeanCopierSupport.copy(nestedHeavyMap, new Target());
            FastBeanCopierSupport.copy(nestedOnlyMap, new NestedOnlyTarget());
            FastBeanCopierSupport.copy(complexSource, new BenchmarkExtendableEntity());
            FastBeanCopierSupport.copy(extendableSource, new HashMap<>());
        }

        private FastBeanCopierBackend createBackend(String backend) {
            if ("reflect".equalsIgnoreCase(backend)) {
                return new ReflectFastBeanCopierBackend();
            }
            if ("reflection-accessor".equalsIgnoreCase(backend)) {
                return new ReflectionAccessorFastBeanCopierBackend();
            }
            if ("asm-accessor".equalsIgnoreCase(backend)) {
                return new AsmAccessorFastBeanCopierBackend();
            }
            return new JavassistFastBeanCopierBackend();
        }

        private SimpleSource createSimpleSource() {
            SimpleSource source = new SimpleSource();
            source.setName("simple-benchmark");
            source.setAge(18);
            source.setEnabled(true);
            source.setScore(88L);
            source.setCreated(new Date(1715040000000L));
            return source;
        }

        private Source createComplexSource() {
            Source source = new Source();
            source.setName("benchmark");
            source.setIds(new String[]{"1", "2", "3"});
            source.setBoy(false);
            source.setBoy2(true);
            source.setBoy3(true);
            source.setAge(100);
            source.setAge2(2);
            source.setAge3(3);
            source.setDeleteTime(new Date(1715040000000L));
            source.setCreateTime(new Date(1715043600000L));
            source.setUpdateTime("2024-05-07 10:20:30");
            source.setNestObject(new NestObject("nest-1", 10, "1234567"));
            source.setNestObjects(Arrays.asList(
                new NestObject("nest-a", 1, "1234567"),
                new NestObject("nest-b", 2, "1234567")
            ));
            Map<String, Object> nestedMap = new LinkedHashMap<>();
            nestedMap.put("name", "map-nest");
            nestedMap.put("age", 11);
            source.setNestObject2(nestedMap);
            source.setNestObject3(new NestObject("nest-3", 12, "1234567"));
            source.setColor(Color.RED);
            source.setColor2("RED");
            source.setColor3(Color.BLUE.getValue());
            source.setArr(Arrays.asList("2", "3"));
            source.setArr4(Arrays.asList("4", "5"));
            source.setArr2(new String[]{"6", "7"});
            source.setArr3(new String[]{"8", "9"});
            source.setArr5(new String[]{"10", "11"});
            source.setArr6(new String[]{"12", "13"});
            source.setArr7(new int[]{14, 15});
            source.setArr8(new int[]{16, 17});
            source.setColors(new Color[]{Color.BLUE, Color.RED});
            source.setSource("source-field");
            source.setTarget("target-field");
            return source;
        }

        private Map<String, Object> createHeterogeneousMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", "heterogeneous");
            map.put("ids", Arrays.asList("1", "2", "3"));
            map.put("boy", "true");
            map.put("boy2", Boolean.FALSE);
            map.put("boy3", 1);
            map.put("age", "123");
            map.put("age2", 456L);
            map.put("age3", 789);
            map.put("deleteTime", "2024-05-07 10:20:30");
            map.put("createTime", new Date(1715043600000L));
            map.put("updateTime", "2024-05-08 11:30:40");
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
            List<Map<String, Object>> nestObjects = new ArrayList<>();
            nestObjects.add(new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-1");
                put("age", "21");
                put("password", "1234567");
            }});
            nestObjects.add(new LinkedHashMap<String, Object>() {{
                put("name", "list-nest-2");
                put("age", 22);
                put("password", "7654321");
            }});
            map.put("nestObjects", nestObjects);
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

        private Map<String, Object> createConversionHeavyMap() {
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

        private Map<String, Object> createCollectionHeavyMap() {
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

        private Map<String, Object> createNestedHeavyMap() {
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

        private Map<String, Object> createNestedOnlyMap() {
            Map<String, Object> map = new LinkedHashMap<>();
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
            return map;
        }
    }

    public static class SimpleSource {
        private String name;
        private int age;
        private boolean enabled;
        private long score;
        private Date created;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getScore() {
            return score;
        }

        public void setScore(long score) {
            this.score = score;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }
    }

    public static class SimpleTarget {
        private String name;
        private int age;
        private boolean enabled;
        private long score;
        private Date created;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getScore() {
            return score;
        }

        public void setScore(long score) {
            this.score = score;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }
    }

    public static class BenchmarkExtendableEntity extends DefaultExtendable {
        private String name;
        private boolean boy2;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isBoy2() {
            return boy2;
        }

        public void setBoy2(boolean boy2) {
            this.boy2 = boy2;
        }
    }

    public static class NestedOnlyTarget {
        private NestObject nestObject;
        private NestObject nestObject2;
        private Map<String, Object> nestObject3;
        private List<Map<String, Object>> nestObjects;

        public NestObject getNestObject() {
            return nestObject;
        }

        public void setNestObject(NestObject nestObject) {
            this.nestObject = nestObject;
        }

        public NestObject getNestObject2() {
            return nestObject2;
        }

        public void setNestObject2(NestObject nestObject2) {
            this.nestObject2 = nestObject2;
        }

        public Map<String, Object> getNestObject3() {
            return nestObject3;
        }

        public void setNestObject3(Map<String, Object> nestObject3) {
            this.nestObject3 = nestObject3;
        }

        public List<Map<String, Object>> getNestObjects() {
            return nestObjects;
        }

        public void setNestObjects(List<Map<String, Object>> nestObjects) {
            this.nestObjects = nestObjects;
        }
    }
}
