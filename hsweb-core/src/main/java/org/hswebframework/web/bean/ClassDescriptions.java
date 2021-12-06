package org.hswebframework.web.bean;

import org.jctools.maps.NonBlockingHashMap;

import java.util.Map;

public class ClassDescriptions {

    private static final Map<Class<?>, ClassDescription> CACHE = new NonBlockingHashMap<>();

    public static ClassDescription getDescription(Class<?> type) {
        return CACHE.computeIfAbsent(type, ClassDescription::new);
    }


}
