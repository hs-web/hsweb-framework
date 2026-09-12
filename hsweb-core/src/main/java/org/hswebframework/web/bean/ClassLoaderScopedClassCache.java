package org.hswebframework.web.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 按 classloader 分段的 Class 维度强缓存。
 * <p>
 * 普通稳定 classloader 直接走强缓存，动态 classloader 场景也保持强缓存命中，
 * 通过 {@link #clear(ClassLoader)} 显式回收对应分段，避免弱/软引用在热路径上导致缓存重建。
 *
 * @param <V> value type
 * @author zhouhao
 * @since 5.0.2
 */
final class ClassLoaderScopedClassCache<V> {

    private final Map<Class<?>, V> stableCache = new ConcurrentHashMap<>();
    private final Map<ClassLoader, Map<Class<?>, V>> volatileCache = new ConcurrentHashMap<>();

    V computeIfAbsent(Class<?> type, Function<Class<?>, V> mappingFunction) {
        if (!FastBeanCopierSupport.isVolatileClassLoaderType(type)) {
            return stableCache.computeIfAbsent(type, mappingFunction);
        }
        ClassLoader loader = FastBeanCopierSupport.getClassLoader(type);
        return volatileCache
            .computeIfAbsent(loader, ignore -> new ConcurrentHashMap<>())
            .computeIfAbsent(type, mappingFunction);
    }

    void clear() {
        stableCache.clear();
        volatileCache.clear();
    }

    void clear(ClassLoader loader) {
        if (loader == null) {
            clear();
            return;
        }
        stableCache.keySet().removeIf(type -> FastBeanCopierSupport.isClassLoaderMatch(type, loader));
        volatileCache.remove(loader);
    }
}
