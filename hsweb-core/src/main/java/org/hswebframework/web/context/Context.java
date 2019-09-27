package org.hswebframework.web.context;

import java.util.Map;
import java.util.Optional;

public interface Context {

    <T> Optional<T> get(ContextKey<T> key);

    <T> T getOrDefault(ContextKey<T> key, T defaultValue);

    <T> void put(ContextKey<T> key, T value);

    Map<String,Object> getAll();
}
