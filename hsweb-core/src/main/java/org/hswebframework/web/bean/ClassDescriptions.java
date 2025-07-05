package org.hswebframework.web.bean;


import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDescriptions {

    private static final Map<Class<?>, ClassDescription> CACHE = new ConcurrentReferenceHashMap<>();

    private static final Map<Class<?>, ClassDescription> CACHE0 = new ConcurrentHashMap<>();

    private static final ClassLoader owner = ClassDescriptions.class.getClassLoader();

    public static ClassDescription getDescription(Class<?> type) {
        if (type.getClassLoader() == owner) {
            return CACHE0.computeIfAbsent(type, ClassDescription::new);
        }
        return CACHE.computeIfAbsent(type, ClassDescription::new);
    }


}
