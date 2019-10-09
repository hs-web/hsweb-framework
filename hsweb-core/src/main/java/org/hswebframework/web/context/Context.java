package org.hswebframework.web.context;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface Context {

    default <T> Optional<T> get(Class<T> key) {
        return get(ContextKey.of(key));
    }

    default <T> void put(Class<T> key, T value) {
        put(ContextKey.of(key), value);
    }

    default <T> void put(String key, T value) {
        put(ContextKey.of(key), value);
    }

    <T> Optional<T> get(ContextKey<T> key);

    <T> T getOrDefault(ContextKey<T> key, Supplier<? extends T> defaultValue);

    <T> void put(ContextKey<T> key, T value);

    <T> T remove(ContextKey<T> key);

    Map<String, Object> getAll();

    void clean();

}
