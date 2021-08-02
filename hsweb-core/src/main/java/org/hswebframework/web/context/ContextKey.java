package org.hswebframework.web.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class ContextKey<T> {

    @Getter
    private final String key;

    public static <T> ContextKey<T> of(String key) {
        return new ContextKey<>(key);
    }

    public static <T> ContextKey<T> of(Class<T> key) {
        return new ContextKey<>(key.getName());
    }

    public static ContextKey<String> string(String key) {
        return of(key);
    }

    public static ContextKey<Integer> integer(String key) {
        return of(key);
    }

    public static ContextKey<Boolean> bool(String key) {
        return of(key);
    }
}
