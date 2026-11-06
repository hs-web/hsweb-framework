package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class FastBeanCopierReadOnlyTest {

    @Test
    public void getterOnlySourceCopiesToMapAndWritableBean() {
        ImmutableSource source = new ImmutableSource("one", 12, false, null);

        Assert.assertEquals(Map.of("name", "one", "age", 12, "enabled", false),
                            FastBeanCopier.copy(source, new LinkedHashMap<>()));
        FastBeanCopierBeanTest.StandardBean target = FastBeanCopier.copy(
            source, new FastBeanCopierBeanTest.StandardBean());
        Assert.assertEquals("one", target.getName());
        Assert.assertEquals(12, target.getAge());
        Assert.assertFalse(target.isEnabled());
    }

    @Test
    public void getterOnlySourceCopiesToRecord() {
        Snapshot target = FastBeanCopier.copy(new ImmutableSource("record", 21, true, null), Snapshot.class);

        Assert.assertEquals(new Snapshot("record", 21, true), target);
    }

    @Test
    public void inheritedGettersAndIgnoreRulesRemainEffective() {
        DerivedSource source = new DerivedSource();

        Assert.assertEquals(Map.of("age", 12, "enabled", true, "tag", "inherited"),
                            FastBeanCopier.copy(source, new LinkedHashMap<>(), "name", "unused"));
    }

    @Test
    public void readOnlyTargetIsNotWritten() {
        PartialTarget target = FastBeanCopier.copy(new ImmutableSource("changed", 22, true, null), new PartialTarget());

        Assert.assertEquals("retained", target.getName());
        Assert.assertEquals(22, target.getAge());
        Assert.assertSame(target, FastBeanCopier.copy(Map.of("name", "changed"), target));
        Assert.assertEquals("retained", target.getName());
    }

    @Test
    public void staticGettersAreNotSourceProperties() {
        Assert.assertTrue(FastBeanCopier.copy(new StaticSource(), new LinkedHashMap<>()).isEmpty());
    }

    @Getter
    @AllArgsConstructor
    public static class ImmutableSource {
        private final String name;
        private final int age;
        private final boolean enabled;
        private final String missing;
    }

    public static class DerivedSource extends ImmutableSource {
        public DerivedSource() {
            super("base", 12, true, null);
        }

        public String getTag() {
            return "inherited";
        }

        public String getUnused() {
            throw new AssertionError("Ignored getter must not be evaluated");
        }
    }

    public record Snapshot(String name, int age, boolean enabled) {
    }

    public static class PartialTarget {
        private int age;

        public String getName() {
            return "retained";
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class StaticSource {
        public static String getName() {
            throw new AssertionError("Static getter must not be evaluated");
        }
    }
}
