package org.hswebframework.web.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("all")
class MapContext implements Context {

    private Map<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(ContextKey<T> key) {
        return Optional.ofNullable(map.get(key.getKey()))
                .map(v -> ((T) v));
    }

    @Override
    public <T> T getOrDefault(ContextKey<T> key, Supplier<? extends T> defaultValue) {
        return (T) map.computeIfAbsent(key.getKey(), __ -> defaultValue.get());
    }

    @Override
    public <T> void put(ContextKey<T> key, T value) {
        map.put(key.getKey(), value);
    }

    @Override
    public <T> T remove(ContextKey<T> key) {
        return (T)map.remove(key);
    }

    @Override
    public Map<String, Object> getAll() {
        return new HashMap<>(map);
    }

    @Override
    public void clean() {
        map.clear();
    }

}
