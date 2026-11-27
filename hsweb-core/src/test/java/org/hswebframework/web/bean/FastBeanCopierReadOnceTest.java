package org.hswebframework.web.bean;

import org.hswebframework.ezorm.core.DefaultExtendable;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class FastBeanCopierReadOnceTest {

    @Test
    public void changingGetterIsReadOnceForMapBeanRecordAndExtension() {
        ChangingSource source = new ChangingSource();
        Assert.assertEquals(Map.of("name", "first"), FastBeanCopier.copy(source, new LinkedHashMap<>()));
        Assert.assertEquals(1, source.reads);

        source = new ChangingSource();
        Assert.assertEquals("first", FastBeanCopier.copy(source, new NameTarget()).getName());
        Assert.assertEquals(1, source.reads);

        source = new ChangingSource();
        Assert.assertEquals(new NameRecord("first"), FastBeanCopier.copy(source, NameRecord.class));
        Assert.assertEquals(1, source.reads);

        source = new ChangingSource();
        Assert.assertEquals("first", FastBeanCopier.copy(source, new DefaultExtendable()).getExtension("name"));
        Assert.assertEquals(1, source.reads);
    }

    @Test
    public void nullSourceValueIsReadOnceAndDoesNotOverwriteTarget() {
        NullSource source = new NullSource();
        Map<String, Object> target = new LinkedHashMap<>(Map.of("name", "retained"));

        Assert.assertEquals(Map.of("name", "retained"), FastBeanCopier.copy(source, target));
        Assert.assertEquals(1, source.reads);
    }

    @Test
    public void mapValueIsReadOnceForWritableBean() {
        ChangingMap source = new ChangingMap();

        Assert.assertEquals("first", FastBeanCopier.copy(source, new NameTarget()).getName());
        Assert.assertEquals(1, source.reads);
    }

    @Test
    public void excludedGettersAreNotInvoked() {
        BrokenSource source = new BrokenSource();

        Assert.assertEquals(Map.of("name", "ok"),
                            FastBeanCopier.copy(source, new LinkedHashMap<>(), "debug"));
        Assert.assertEquals(Map.of("name", "ok"),
                            FastBeanCopier.copy(source, new LinkedHashMap<>(), FastBeanCopier.include("name")));
    }

    @Test
    public void getterErrorIsPropagatedWithoutWrapping() {
        BrokenSource source = new BrokenSource();

        AssertionError thrown = Assert.assertThrows(AssertionError.class,
            () -> FastBeanCopier.copy(source, new LinkedHashMap<>()));
        Assert.assertSame(source.error, thrown);
    }

    @Test
    public void nestedErrorIsReadOnceAndRetainsIdentity() {
        ErrorSource source = new ErrorSource();

        Map<String, Object> copied = FastBeanCopier.copy(source, new LinkedHashMap<>());

        Assert.assertSame(source.error, copied.get("error"));
        Assert.assertEquals(1, source.reads);
    }

    @Test
    public void sourceValuesWithOpaqueTypesAndMultidimensionalArraysStayShallow() {
        OpaqueSource source = new OpaqueSource();

        Map<String, Object> copied = FastBeanCopier.copy(source, new LinkedHashMap<>());

        Assert.assertSame(source.value, copied.get("value"));
        Assert.assertSame(source.matrix, copied.get("matrix"));
    }

    @Test
    public void throwableMapUsesTextDetailForCyclicCause() {
        Error error = new AssertionError("outer");
        Exception cause = new IllegalStateException("cause");
        error.initCause(cause);
        cause.initCause(error);

        Map<String, Object> copied = FastBeanCopier.copy(error, new LinkedHashMap<>());

        Assert.assertEquals("outer", copied.get("message"));
        Assert.assertEquals(error.getClass().getName(), copied.get("type"));
        Assert.assertTrue(((String) copied.get("detail")).contains("CIRCULAR REFERENCE"));
        Assert.assertFalse(copied.containsKey("cause"));
        Assert.assertFalse(copied.containsKey("stackTrace"));
        Assert.assertFalse(copied.containsKey("suppressed"));
    }

    public static class ChangingSource {
        int reads;

        public String getName() {
            return ++reads == 1 ? "first" : null;
        }
    }

    public static class NullSource {
        int reads;

        public String getName() {
            reads++;
            return null;
        }
    }

    public static class NameTarget {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public record NameRecord(String name) {
    }

    public static class ChangingMap extends LinkedHashMap<String, Object> {
        int reads;

        @Override
        public Object get(Object key) {
            return ++reads == 1 ? "first" : null;
        }
    }

    public static class BrokenSource {
        final AssertionError error = new AssertionError("getter failed");

        public String getName() {
            return "ok";
        }

        public String getDebug() {
            throw error;
        }
    }

    public static class ErrorSource {
        final Error error = new AssertionError("nested");
        int reads;

        public Error getError() {
            reads++;
            return error;
        }
    }

    private enum HiddenValue {
        VALUE
    }

    public static class OpaqueSource {
        final HiddenValue value = HiddenValue.VALUE;
        final int[][] matrix = {{1, 2}};

        public HiddenValue getValue() {
            return value;
        }

        public int[][] getMatrix() {
            return matrix;
        }
    }
}
