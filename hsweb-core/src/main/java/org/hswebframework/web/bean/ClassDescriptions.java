package org.hswebframework.web.bean;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDescriptions {

    private static final Map<Class<?>, ClassDescription> CACHE = new ConcurrentHashMap<>();

    public static ClassDescription getDescription(Class<?> type) {
        return CACHE.computeIfAbsent(type, ClassDescription::new);
    }


}
