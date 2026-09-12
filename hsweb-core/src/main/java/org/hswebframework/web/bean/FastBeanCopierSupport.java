package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * FastBeanCopier core support.
 *
 * @author zhouhao
 * @since 3.0
 */
public final class FastBeanCopierSupport {
    private static final Map<CacheKey, Copier> CACHE = new ConcurrentHashMap<>();
    private static final Map<CacheKey, RecordCopier> RECORD_CACHE = new ConcurrentHashMap<>();
    private static final Map<CacheKey, FastBeanCopierBackend> BACKEND_CACHE = new ConcurrentHashMap<>();
    /**
     * 动态 classloader 也保持强缓存命中，避免弱/软引用在内存波动时触发 copier 重建。
     * 对应 classloader 的释放由 clearCache(ClassLoader) 显式管理。
     */
    private static final Map<CacheKey, Copier> VOLATILE_CACHE = new ConcurrentHashMap<>();
    private static final ClassLoader OWNER_CLASS_LOADER = FastBeanCopierSupport.class.getClassLoader();
    private static final FastBeanCopierBackend VOLATILE_CLASS_LOADER_BACKEND =
        new ReflectionAccessorFastBeanCopierBackend();

    @SuppressWarnings("all")
    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private static BeanFactory BEAN_FACTORY;

    private static FastBeanCopierBackend BACKEND;

    public static final DefaultConverter DEFAULT_CONVERT;

    public static void setBeanFactory(BeanFactory beanFactory) {
        BEAN_FACTORY = beanFactory;
        DEFAULT_CONVERT.setBeanFactory(beanFactory);
    }

    public static BeanFactory getBeanFactory() {
        return BEAN_FACTORY;
    }

    public static void setBackend(FastBeanCopierBackend backend) {
        BACKEND = Objects.requireNonNull(backend, "backend");
        clearCache();
    }

    public static FastBeanCopierBackend getBackend() {
        return BACKEND;
    }

    static FastBeanCopierBackend getEffectiveBackend(Class<?> source, Class<?> target) {
        if (usesVolatileClassLoader(source, target)) {
            return adaptBackend(BACKEND, source, target);
        }
        CacheKey key = createCacheKey(source, target);
        return BACKEND_CACHE.computeIfAbsent(key, ignore -> adaptBackend(BACKEND, source, target));
    }

    static {
        BEAN_FACTORY = new BeanFactory() {
            @Override
            @SneakyThrows
            @SuppressWarnings("all")
            public <T> T newInstance(Class<T> beanType) {
                return beanType == Map.class ? (T) new HashMap<>() : beanType.getDeclaredConstructor().newInstance();
            }
        };
        BACKEND = FastBeanCopierBackendSelector.selectDefaultBackend();
        DEFAULT_CONVERT = new DefaultConverter();
        DEFAULT_CONVERT.setBeanFactory(BEAN_FACTORY);
    }

    @SuppressWarnings("all")
    public static Set<String> include(String... includeProperties) {
        return new HashSet<String>(Arrays.asList(includeProperties)) {
            @Override
            public boolean contains(Object o) {
                return !super.contains(o);
            }
        };
    }

    public static Object getProperty(Object source, String key) {
        if (source instanceof Map) {
            return ((Map<?, ?>) source).get(key);
        }
        SingleValueMap<Object, Object> map = new SingleValueMap<>();
        copy(source, map, include(key));
        return map.getValue();
    }

    public static <T, S> T copy(S source, T target, String... ignore) {
        return copy(source, target, DEFAULT_CONVERT, ignore);
    }

    public static <T, S> T copy(S source, Supplier<T> target, String... ignore) {
        return copy(source, target.get(), DEFAULT_CONVERT, ignore);
    }

    @SneakyThrows
    public static <T, S> T copy(S source, Class<T> target, String... ignore) {
        if (target.isRecord()) {
            return copyToRecord(source, target, DEFAULT_CONVERT, ignore);
        }
        return copy(source, target.getDeclaredConstructor().newInstance(), DEFAULT_CONVERT, ignore);
    }

    public static <T, S> T copy(S source, T target, Converter converter, String... ignore) {
        return copy(source,
                    target,
                    converter,
                    (ignore == null || ignore.length == 0)
                        ? Collections.emptySet()
                        : new HashSet<>(Arrays.asList(ignore)));
    }

    public static <T, S> T copy(S source, T target, Set<String> ignore) {
        return copy(source, target, DEFAULT_CONVERT, ignore);
    }

    @SuppressWarnings("all")
    public static <T, S> T copy(S source, T target, Converter converter, Set<String> ignore) {
        if (target != null && getUserClass(target).isRecord()) {
            // record 不可变，无法写入已存在实例；这里按组件名重新构造并返回新实例。
            return (T) copyToRecord(source, (Class) getUserClass(target), converter, ignore);
        }
        if (source instanceof Map && target instanceof Map) {
            if (CollectionUtils.isEmpty(ignore)) {
                ((Map) target).putAll(((Map) source));
            } else {
                ((Map) source)
                    .forEach((k, v) -> {
                        if (!ignore.contains(k)) {
                            ((Map) target).put(k, v);
                        }
                    });
            }
            return target;
        }

        getCopier(source, target, true)
            .copy(source, target, ignore, converter);
        return target;
    }

    static <T, S> T copyToRecord(S source, Class<T> target, Converter converter, String... ignore) {
        Set<String> ignored = (ignore == null || ignore.length == 0)
            ? Collections.emptySet()
            : new HashSet<>(Arrays.asList(ignore));
        return copyToRecord(source, target, converter, ignored);
    }

    @SuppressWarnings("unchecked")
    static <T, S> T copyToRecord(S source, Class<T> target, Converter converter, Set<String> ignore) {
        return (T) getRecordCopier(getUserClass(source), target)
            .copy(source, ignore == null ? Collections.emptySet() : ignore, converter);
    }

    private static RecordCopier getRecordCopier(Class<?> source, Class<?> target) {
        CacheKey key = createCacheKey(source, target);
        return RECORD_CACHE.computeIfAbsent(key, k -> RecordBeanCopierSupport.createRecordCopier(k.sourceType, k.targetType));
    }

    static Class<?> getUserClass(Object object) {
        if (object instanceof Map) {
            return Map.class;
        }
        Class<?> type = ClassUtils.getUserClass(object);

        if (java.lang.reflect.Proxy.isProxyClass(type)) {
            Class<?>[] interfaces = type.getInterfaces();
            return interfaces[0];
        }

        return type;
    }

    public static Copier getCopier(Object source, Object target, boolean autoCreate) {
        Class<?> sourceType = getUserClass(source);
        Class<?> targetType = getUserClass(target);
        CacheKey key = createCacheKey(sourceType, targetType);
        if (usesVolatileClassLoader(sourceType, targetType)) {
            return getVolatileCopier(key, autoCreate);
        }
        if (autoCreate) {
            return CACHE.computeIfAbsent(key, k -> createCopier(k.sourceType, k.targetType));
        }
        return CACHE.get(key);
    }

    private static CacheKey createCacheKey(Class<?> source, Class<?> target) {
        return new CacheKey(source, target);
    }

    public static Copier createCopier(Class<?> source, Class<?> target) {
        return getEffectiveBackend(source, target).createCopier(source, target);
    }

    static boolean usesVolatileClassLoader(Class<?>... types) {
        for (Class<?> type : types) {
            if (isVolatileClassLoaderType(type)) {
                return true;
            }
        }
        return false;
    }

    static boolean isClassLoaderMatch(Class<?> type, ClassLoader loader) {
        return loader != null && getClassLoader(type) == loader;
    }

    static ClassLoader getClassLoader(Class<?> type) {
        if (type == null) {
            return null;
        }
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type.isPrimitive() ? null : type.getClassLoader();
    }

    public static Object unwrapEnumDictValue(Object value, Class<?> targetType) {
        if (!(value instanceof EnumDict) || targetType.isEnum()) {
            return value;
        }
        if (Number.class.isAssignableFrom(targetType)
            || (targetType.isPrimitive() && targetType != boolean.class && targetType != char.class)) {
            Object enumValue = ((EnumDict<?>) value).getValue();
            return enumValue == null ? value : enumValue;
        }
        return value;
    }

    static void clearCache() {
        CACHE.clear();
        RECORD_CACHE.clear();
        BACKEND_CACHE.clear();
        VOLATILE_CACHE.clear();
        AccessorFastBeanCopierBackend.clearCache();
        FastBeanCopierConverterSupport.clearCache();
        ClassDescriptions.clearCache();
    }

    public static void clearCache(ClassLoader loader) {
        if (loader == null) {
            clearCache();
            return;
        }
        removeCacheEntries(CACHE, loader);
        removeCacheEntries(RECORD_CACHE, loader);
        removeCacheEntries(BACKEND_CACHE, loader);
        removeVolatileCacheEntries(loader);
        AccessorFastBeanCopierBackend.clearCache(loader);
        FastBeanCopierConverterSupport.clearCache(loader);
        ClassDescriptions.clearCache(loader);
    }

    private static FastBeanCopierBackend adaptBackend(FastBeanCopierBackend backend, Class<?> source, Class<?> target) {
        if (usesVolatileClassLoader(source, target)) {
            if (backend instanceof AsmAccessorFastBeanCopierBackend || backend instanceof JavassistFastBeanCopierBackend) {
                return VOLATILE_CLASS_LOADER_BACKEND;
            }
            return backend;
        }
        if (!FastBeanCopierBackendSelector.isRuntimeCodeGenerationDisabled()) {
            return backend;
        }
        if (backend instanceof AsmAccessorFastBeanCopierBackend || backend instanceof JavassistFastBeanCopierBackend) {
            return new ReflectionAccessorFastBeanCopierBackend();
        }
        return backend;
    }

    public static final class DefaultConverter implements Converter {
        private final FastBeanCopierConverterSupport support = new FastBeanCopierConverterSupport();

        public void setBeanFactory(BeanFactory beanFactory) {
            support.setBeanFactory(beanFactory);
        }

        public Collection<?> newCollection(Class<?> targetClass) {
            return support.newCollection(targetClass);
        }

        @Override
        public <T> T convert(Object source, Class<T> targetClass, Class[] genericType) {
            return support.convert(source, targetClass, genericType);
        }
    }

    private static Copier getVolatileCopier(CacheKey key, boolean autoCreate) {
        if (!autoCreate) {
            return VOLATILE_CACHE.get(key);
        }
        return VOLATILE_CACHE.computeIfAbsent(key, ignore -> createCopier(key.sourceType, key.targetType));
    }

    private static void removeCacheEntries(Map<CacheKey, ?> cache, ClassLoader loader) {
        cache.keySet().removeIf(key -> key.involves(loader));
    }

    private static void removeVolatileCacheEntries(ClassLoader loader) {
        removeCacheEntries(VOLATILE_CACHE, loader);
    }

    static boolean isVolatileClassLoaderType(Class<?> type) {
        ClassLoader loader = getClassLoader(type);
        if (loader == null || loader == OWNER_CLASS_LOADER) {
            return false;
        }
        if (OWNER_CLASS_LOADER == null) {
            return true;
        }
        try {
            return Class.forName(type.getName(), false, OWNER_CLASS_LOADER) != type;
        } catch (Throwable ignore) {
            return true;
        }
    }

    static int getStableCacheSize() {
        return CACHE.size();
    }

    static int getVolatileCacheSize() {
        return VOLATILE_CACHE.size();
    }

    @AllArgsConstructor
    public static class CacheKey {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey target = ((CacheKey) obj);
            return target.targetType == targetType && target.sourceType == sourceType;
        }

        @Override
        public int hashCode() {
            int result = this.targetType != null ? this.targetType.hashCode() : 0;
            result = 31 * result + (this.sourceType != null ? this.sourceType.hashCode() : 0);
            return result;
        }

        private boolean involves(ClassLoader loader) {
            return isClassLoaderMatch(sourceType, loader) || isClassLoaderMatch(targetType, loader);
        }
    }

}
