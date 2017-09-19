package org.hswebframework.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * map工具类，用于构造map等操作
 * <p>
 * <pre>
 *    Maps.<String, Object>buildMap()
 *      .put("name", "age")
 *      .put("age", 1)
 *      .get()
 * </pre>
 *
 * @author zhouhao
 */
public final class Maps {

    private Maps() {
    }

    public static <K, V> MapBuilder<K, V> buildMap(Map<K, V> target) {
        return new MapBuilder<>(target);
    }

    public static <K, V> MapBuilder<K, V> buildMap() {
        return new MapBuilder<>(new HashMap<K, V>());
    }

    public static <K, V> MapBuilder<K, V> buildMap(Supplier<Map<K, V>> mapSupplier) {
        return new MapBuilder<>(mapSupplier.get());
    }

    public static class MapBuilder<K, V> {
        final Map<K, V> target;

        private MapBuilder(Map<K, V> target) {
            Objects.requireNonNull(target);
            this.target = target;
        }

        public MapBuilder<K, V> put(K key, V value) {
            this.target.put(key, value);
            return this;
        }

        public Map<K, V> get() {
            return target;
        }
    }
}
