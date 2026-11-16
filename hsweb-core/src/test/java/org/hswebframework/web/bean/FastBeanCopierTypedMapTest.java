package org.hswebframework.web.bean;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class FastBeanCopierTypedMapTest {
    @Test
    public void recordComponentNamesCannotCollideWithGeneratedSourceOrValueVariables() {
        var result = FastBeanCopier.copy(Map.of("source", "input", "value", Map.of("limit", 100)), Binding.class);
        assertEquals("input", result.source());
        assertEquals(Map.of("limit", 100), result.value());
        var copied = FastBeanCopier.copy(result, Binding.class);
        assertEquals(result, copied);
    }

    @Test
    public void recordMapsRestoreTypedKeysAndValuesWithoutChangingTheSource() {
        Map<String, Object> entries = new LinkedHashMap<>();
        entries.put("2", Map.of("name", "second"));
        entries.put("1", null);
        var result = FastBeanCopier.copy(Map.of("entries", entries), Envelope.class);
        assertEquals("second", result.entries().get(2).name());
        assertTrue(result.entries().containsKey(1));
        assertNull(result.entries().get(1));
        assertTrue(result.entries() instanceof LinkedHashMap);
        assertTrue(entries.get("2") instanceof Map);
        assertEquals(java.util.List.of(2, 1), java.util.List.copyOf(result.entries().keySet()));
    }

    @Test
    public void typedAndUntypedMapValuesRemainShared() {
        var item = new Item("existing");
        Map<Integer, Item> entries = Map.of(1, item);
        var typed = FastBeanCopier.copy(Map.of("entries", entries), Envelope.class);
        assertSame(item, typed.entries().get(1));
        assertNotSame(entries, typed.entries());
        Map<String, Object> fields = Map.of("payload", Map.of("nested", 1));
        var raw = FastBeanCopier.copy(Map.of("fields", fields), Fields.class);
        assertSame(fields.get("payload"), raw.fields().get("payload"));
    }

    @Test
    public void converterRetainsSortedAndConcurrentMapImplementations() {
        var sorted = new TreeMap<String, Object>(java.util.Comparator.reverseOrder());
        sorted.put("a", Map.of("name", "first"));
        sorted.put("b", Map.of("name", "second"));
        Map<?, ?> converted = FastBeanCopier.DEFAULT_CONVERT.convert(sorted, Map.class, new Class[]{String.class, Item.class});
        assertTrue(converted instanceof TreeMap);
        assertEquals("b", converted.keySet().iterator().next());
        assertEquals("second", ((Item) converted.get("b")).name());
        var concurrent = new ConcurrentHashMap<String, Object>(sorted);
        assertTrue(FastBeanCopier.DEFAULT_CONVERT.convert(concurrent, Map.class,
            new Class[]{String.class, Item.class}) instanceof ConcurrentHashMap);
    }

    public record Item(String name) { }
    public record Envelope(Map<Integer, Item> entries) { }
    public record Fields(Map<String, Object> fields) { }
    public record Binding(String source, Object value) { }
}
