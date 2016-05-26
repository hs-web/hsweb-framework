package org.hsweb.web.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-5-26.
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<Map<String, Object>> local = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    public static <T> T put(String key, T value) {
        local.get().put(key, value);
        return value;
    }

    public static <T> T get(String key) {
        return ((T) local.get().get(key));
    }
}
