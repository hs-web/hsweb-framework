package org.hswebframework.web.bean;

public class ClassDescriptions {

    private static final ClassLoaderScopedClassCache<ClassDescription> CACHE = new ClassLoaderScopedClassCache<>();

    public static ClassDescription getDescription(Class<?> type) {
        return CACHE.computeIfAbsent(type, ClassDescription::new);
    }

    static void clearCache() {
        CACHE.clear();
    }

    static void clearCache(ClassLoader loader) {
        CACHE.clear(loader);
    }


}
