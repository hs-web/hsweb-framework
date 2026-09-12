package org.hswebframework.web.bean;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.utils.DynamicArrayList;
import org.springframework.util.NumberUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
final class FastBeanCopierConverterSupport implements Converter {
    private static final ConvertUtilsBean CONVERT_UTILS = BeanUtilsBean.getInstance().getConvertUtils();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();
    private static final org.apache.commons.beanutils.Converter NO_CONVERTER = new org.apache.commons.beanutils.Converter() {
        @Override
        public <T> T convert(Class<T> type, Object value) {
            return null;
        }
    };
    private static final ClassLoaderScopedClassCache<org.apache.commons.beanutils.Converter> APACHE_CONVERTER_CACHE =
        new ClassLoaderScopedClassCache<>();
    private static final ClassLoaderScopedClassCache<Map<String, Object>> ENUM_LOOKUP_CACHE =
        new ClassLoaderScopedClassCache<>();
    private static final ClassLoaderScopedClassCache<Boolean> BEAN_LIKE_TARGET_CACHE =
        new ClassLoaderScopedClassCache<>();
    private static final ClassLoaderScopedClassCache<CollectionFactory> COLLECTION_FACTORY_CACHE =
        new ClassLoaderScopedClassCache<>();
    private static final Map<PlanKey, ConversionPlan> PLAN_CACHE = new ConcurrentHashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPERS.put(short.class, Short.class);
        PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVE_WRAPPERS.put(float.class, Float.class);
        PRIMITIVE_WRAPPERS.put(double.class, Double.class);
        PRIMITIVE_WRAPPERS.put(char.class, Character.class);
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(long.class, Long.class);
    }

    private BeanFactory beanFactory;

    static void clearCache() {
        APACHE_CONVERTER_CACHE.clear();
        ENUM_LOOKUP_CACHE.clear();
        BEAN_LIKE_TARGET_CACHE.clear();
        COLLECTION_FACTORY_CACHE.clear();
        PLAN_CACHE.clear();
    }

    static void clearCache(ClassLoader loader) {
        if (loader == null) {
            clearCache();
            return;
        }
        APACHE_CONVERTER_CACHE.clear(loader);
        ENUM_LOOKUP_CACHE.clear(loader);
        BEAN_LIKE_TARGET_CACHE.clear(loader);
        COLLECTION_FACTORY_CACHE.clear(loader);
        PLAN_CACHE.keySet().removeIf(key -> key.involves(loader));
    }

    void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    Collection<?> newCollection(Class<?> targetClass) {
        return getCollectionFactory(targetClass).create();
    }

    @Override
    @SuppressWarnings("all")
    @SneakyThrows
    public <T> T convert(Object source, Class<T> targetClass, Class[] genericType) {
        if (source == null) {
            return null;
        }
        return (T) getPlan(targetClass, genericType).convert(this, source);
    }

    private ConversionPlan getPlan(Class<?> targetClass, Class[] genericType) {
        Class<?>[] normalized = normalizeGenericTypes(genericType);
        return PLAN_CACHE.computeIfAbsent(new PlanKey(targetClass, normalized),
                                          key -> new ConversionPlan(key.targetClass, key.genericTypes));
    }

    private Class<?>[] normalizeGenericTypes(Class[] genericType) {
        if (genericType == null || genericType.length == 0) {
            return FastBeanCopierSupport.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] normalized = new Class<?>[genericType.length];
        System.arraycopy(genericType, 0, normalized, 0, genericType.length);
        return normalized;
    }

    private CollectionFactory getCollectionFactory(Class<?> targetClass) {
        return COLLECTION_FACTORY_CACHE.computeIfAbsent(targetClass, FastBeanCopierConverterSupport::createCollectionFactory);
    }

    @SuppressWarnings("unchecked")
    private static CollectionFactory createCollectionFactory(Class<?> targetClass) {
        if (targetClass == List.class || targetClass == Collection.class) {
            return ArrayList::new;
        }
        if (targetClass == ConcurrentHashMap.KeySetView.class) {
            return () -> (Collection<Object>) ConcurrentHashMap.newKeySet();
        }
        if (targetClass == Set.class) {
            return HashSet::new;
        }
        if (targetClass == Queue.class) {
            return LinkedList::new;
        }
        try {
            Constructor<?> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return () -> {
                try {
                    return (Collection<Object>) constructor.newInstance();
                } catch (Exception e) {
                    throw new UnsupportedOperationException("Unsupported Collection Type:" + targetClass, e);
                }
            };
        } catch (Exception e) {
            return () -> {
                throw new UnsupportedOperationException("Unsupported Collection Type:" + targetClass, e);
            };
        }
    }

    private Collection<?> asCollection(Object source) {
        if (source instanceof Collection) {
            return (Collection<?>) source;
        }
        if (source.getClass().isArray()) {
            return new DynamicArrayList(source);
        }
        if (source instanceof Map) {
            return ((Map<?, ?>) source).values();
        }
        if (source instanceof String) {
            return Arrays.asList(((String) source).split("[,]"));
        }
        return Collections.singletonList(source);
    }

    private Collection<?> convertToCollection(Object source, ConversionPlan plan) {
        Collection<Object> collection = plan.collectionFactory.create();
        Collection<?> sourceCollection = asCollection(source);
        if (plan.elementType != null) {
            for (Object sourceObj : sourceCollection) {
                if (sourceObj == null || isDirectAssignable(plan.elementType, sourceObj)) {
                    collection.add(sourceObj);
                } else {
                    collection.add(convert(sourceObj, plan.elementType, FastBeanCopierSupport.EMPTY_CLASS_ARRAY));
                }
            }
            return collection;
        }
        collection.addAll(sourceCollection);
        return collection;
    }

    private Object convertToArray(Object source, Class<?> componentType) {
        if (source.getClass().isArray() && source.getClass().getComponentType() == componentType) {
            int length = Array.getLength(source);
            Object array = Array.newInstance(componentType, length);
            System.arraycopy(source, 0, array, 0, length);
            return array;
        }
        Collection<?> sourceCollection = asCollection(source);
        Object array = Array.newInstance(componentType, sourceCollection.size());
        int index = 0;
        for (Object value : sourceCollection) {
            Array.set(array,
                      index++,
                      value == null || isDirectAssignable(componentType, value)
                          ? value
                          : convert(value, componentType, FastBeanCopierSupport.EMPTY_CLASS_ARRAY));
        }
        return array;
    }

    private Map<?, ?> convertCollectionToMap(Collection<?> sourceCollection, ConversionPlan plan) {
        Map<Object, Object> map = new LinkedHashMap<>(Math.max((int) (sourceCollection.size() / 0.75F) + 1, 16));
        int i = 0;
        for (Object o : sourceCollection) {
            if (plan.mapKeyType != null && plan.mapValueType != null) {
                map.put(convert(i++, plan.mapKeyType, FastBeanCopierSupport.EMPTY_CLASS_ARRAY),
                        convert(o, plan.mapValueType, FastBeanCopierSupport.EMPTY_CLASS_ARRAY));
            } else {
                map.put(i++, o);
            }
        }
        return map;
    }

    private Map<?, ?> copyMap(Map<?, ?> map) {
        if (map instanceof TreeMap) {
            return new TreeMap<>(map);
        }
        if (map instanceof LinkedHashMap) {
            return new LinkedHashMap<>(map);
        }
        if (map instanceof ConcurrentHashMap) {
            return new ConcurrentHashMap<>(map);
        }
        return new HashMap<>(map);
    }

    private static org.apache.commons.beanutils.Converter lookupApacheConverter(Class<?> targetClass) {
        org.apache.commons.beanutils.Converter converter =
            APACHE_CONVERTER_CACHE.computeIfAbsent(targetClass,
                                                   type -> {
                                                       org.apache.commons.beanutils.Converter found = CONVERT_UTILS.lookup(type);
                                                       return found == null ? NO_CONVERTER : found;
                                                   });
        return converter == NO_CONVERTER ? null : converter;
    }

    private boolean isDirectScalarAssignable(Class<?> targetClass, Object source) {
        if (source instanceof Number || source instanceof Boolean || source instanceof Character || source instanceof Enum) {
            return isDirectAssignable(targetClass, source);
        }
        return false;
    }

    private Object lookupEnum(Class<?> targetClass, Object source) {
        if (source == null) {
            return null;
        }
        if (targetClass.isInstance(source)) {
            return source;
        }
        if (source instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) source;
            Object value = map.get("value");
            Object matched = lookupEnum(targetClass, value);
            return matched != null ? matched : lookupEnum(targetClass, map.get("text"));
        }
        if (source instanceof EnumDict) {
            EnumDict<?> dict = (EnumDict<?>) source;
            Object matched = lookupEnum(targetClass, dict.getValue());
            return matched != null ? matched : lookupEnum(targetClass, dict.getText());
        }
        if (source instanceof Number) {
            source = ((Number) source).intValue();
        }
        String key = normalizeEnumKey(source);
        return ENUM_LOOKUP_CACHE
            .computeIfAbsent(targetClass, this::createEnumLookup)
            .get(key);
    }

    private Map<String, Object> createEnumLookup(Class<?> targetClass) {
        ClassDescription description = ClassDescriptions.getDescription(targetClass);
        Map<String, Object> lookup = new HashMap<>();
        for (Object obj : description.getEnums()) {
            Enum<?> value = (Enum<?>) obj;
            lookup.putIfAbsent(normalizeEnumKey(value.name()), obj);
            lookup.putIfAbsent(normalizeEnumKey(value.ordinal()), obj);
            if (obj instanceof EnumDict) {
                EnumDict<?> dict = (EnumDict<?>) obj;
                lookup.putIfAbsent(normalizeEnumKey(dict.getValue()), obj);
                lookup.putIfAbsent(normalizeEnumKey(dict.getText()), obj);
            }
        }
        return lookup;
    }

    private String normalizeEnumKey(Object value) {
        return String.valueOf(value).trim().toLowerCase(Locale.ROOT);
    }

    private boolean isDirectAssignable(Class<?> targetClass, Object source) {
        if (source == null) {
            return false;
        }
        if (targetClass.isInstance(source)) {
            return true;
        }
        if (targetClass.isPrimitive()) {
            Class<?> wrapper = PRIMITIVE_WRAPPERS.get(targetClass);
            return wrapper != null && wrapper.isInstance(source);
        }
        return false;
    }

    private static boolean isBeanLikeTarget(ClassDescription target, Class<?> targetClass) {
        return BEAN_LIKE_TARGET_CACHE.computeIfAbsent(targetClass,
                                                      ignore -> targetClass != Object.class
                                                          && targetClass != String.class
                                                          && targetClass != CharSequence.class
                                                          && targetClass != Date.class
                                                          && targetClass != Boolean.class
                                                          && targetClass != Character.class
                                                          && targetClass != boolean.class
                                                          && targetClass != char.class
                                                          && !target.isEnumType()
                                                          && !target.isArrayType()
                                                          && !target.isCollectionType()
                                                          && !target.isNumber()
                                                          && !Map.class.isAssignableFrom(targetClass));
    }

    private Number convertNumber(Number source, Class<?> targetClass) {
        Class<?> wrapper = targetClass.isPrimitive() ? PRIMITIVE_WRAPPERS.get(targetClass) : targetClass;
        return NumberUtils.convertNumberToTargetClass(source, (Class<? extends Number>) wrapper);
    }

    private Number convertStringToNumber(String source, Class<?> targetClass) {
        String value = source.trim();
        Class<?> wrapper = targetClass.isPrimitive() ? PRIMITIVE_WRAPPERS.get(targetClass) : targetClass;
        try {
            if (wrapper == Integer.class) {
                return Integer.parseInt(value);
            }
            if (wrapper == Long.class) {
                return Long.parseLong(value);
            }
            if (wrapper == Short.class) {
                return Short.parseShort(value);
            }
            if (wrapper == Byte.class) {
                return Byte.parseByte(value);
            }
            if (wrapper == Double.class) {
                return Double.parseDouble(value);
            }
            if (wrapper == Float.class) {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException ignore) {
            // fallback to Spring NumberUtils for extended formats
        }
        return NumberUtils.parseNumber(value, (Class) targetClass);
    }

    private Boolean convertToBoolean(Object source) {
        if (source instanceof Boolean) {
            return (Boolean) source;
        }
        if (source instanceof Number) {
            return ((Number) source).intValue() != 0;
        }
        String value = String.valueOf(source).trim();
        if (value.isEmpty()) {
            return Boolean.FALSE;
        }
        return "true".equalsIgnoreCase(value)
            || "1".equals(value)
            || "y".equalsIgnoreCase(value)
            || "yes".equalsIgnoreCase(value)
            || "on".equalsIgnoreCase(value);
    }

    private Character convertToCharacter(Object source) {
        if (source instanceof Character) {
            return (Character) source;
        }
        if (source instanceof Number) {
            return (char) ((Number) source).intValue();
        }
        String value = String.valueOf(source);
        return value.isEmpty() ? null : value.charAt(0);
    }

    private interface CollectionFactory {
        Collection<Object> create();
    }

    private final class ConversionPlan {
        private final Class<?> targetClass;
        private final Class<?>[] genericTypes;
        private final ClassDescription target;
        private final org.apache.commons.beanutils.Converter apacheConverter;
        private final CollectionFactory collectionFactory;
        private final Class<?> elementType;
        private final Class<?> mapKeyType;
        private final Class<?> mapValueType;
        private final Class<?> componentType;
        private final boolean stringLike;
        private final boolean objectType;
        private final boolean dateType;
        private final boolean booleanType;
        private final boolean characterType;
        private final boolean collectionType;
        private final boolean enumType;
        private final boolean arrayType;
        private final boolean numberType;
        private final boolean mapInterfaceType;
        private final boolean recordType;
        private final boolean beanLikeTarget;

        private ConversionPlan(Class<?> targetClass, Class<?>[] genericTypes) {
            this.targetClass = targetClass;
            this.genericTypes = genericTypes;
            this.target = ClassDescriptions.getDescription(targetClass);
            this.stringLike = targetClass == String.class || targetClass == CharSequence.class;
            this.objectType = targetClass == Object.class;
            this.dateType = targetClass == Date.class;
            this.booleanType = targetClass == boolean.class || targetClass == Boolean.class;
            this.characterType = targetClass == char.class || targetClass == Character.class;
            this.collectionType = target.isCollectionType();
            this.enumType = target.isEnumType();
            this.arrayType = target.isArrayType();
            this.numberType = target.isNumber();
            this.mapInterfaceType = targetClass == Map.class;
            this.recordType = targetClass.isRecord();
            this.beanLikeTarget = isBeanLikeTarget(target, targetClass);
            this.apacheConverter = lookupApacheConverter(targetClass);
            this.collectionFactory = collectionType ? getCollectionFactory(targetClass) : null;
            this.elementType = genericTypes.length > 0 && genericTypes[0] != Object.class ? genericTypes[0] : null;
            this.mapKeyType = genericTypes.length >= 2 ? genericTypes[0] : null;
            this.mapValueType = genericTypes.length >= 2 ? genericTypes[1] : null;
            this.componentType = arrayType ? targetClass.getComponentType() : null;
        }

        @SuppressWarnings("all")
        private Object convert(FastBeanCopierConverterSupport support, Object source) throws Throwable {
            if (support.isDirectScalarAssignable(targetClass, source)) {
                return source;
            }
            if (enumType && source instanceof EnumDict) {
                Object val = ((EnumDict) source).getValue();
                if (targetClass.isInstance(val)) {
                    return val;
                }
                Object matched = support.lookupEnum(targetClass, val);
                if (matched == null) {
                    matched = support.lookupEnum(targetClass, ((EnumDict<?>) source).getText());
                }
                if (matched != null) {
                    return matched;
                }
                return support.convert(val, targetClass, genericTypes);
            }
            if (stringLike) {
                if (source instanceof Date) {
                    return DateFormatter.toString(((Date) source), "yyyy-MM-dd HH:mm:ss");
                }
                return String.valueOf(source);
            }
            if (objectType) {
                return source;
            }
            if (dateType) {
                if (source instanceof String) {
                    Object parsed = DateFormatter.fromString((String) source);
                    if (parsed == null) {
                        return apacheConverter == null ? null : apacheConverter.convert(Date.class, source);
                    }
                    return parsed;
                }
                if (source instanceof Number) {
                    return new Date(((Number) source).longValue());
                }
                if (source instanceof Date) {
                    return new Date(((Date) source).getTime());
                }
            }
            if (booleanType) {
                return support.convertToBoolean(source);
            }
            if (characterType) {
                return support.convertToCharacter(source);
            }
            if (collectionType) {
                return support.convertToCollection(source, this);
            }
            if (enumType) {
                Object val = support.lookupEnum(targetClass, source);
                if (val != null) {
                    if (targetClass.isInstance(val)) {
                        return val;
                    }
                    return support.convert(val, targetClass, genericTypes);
                }
                log.warn("无法将:{}转为枚举:{}",
                         source,
                         targetClass,
                         new ClassCastException(source + "=>" + targetClass));
                return null;
            }
            if (arrayType) {
                return support.convertToArray(source, componentType);
            }
            if (numberType) {
                if (source instanceof Number) {
                    return support.convertNumber((Number) source, targetClass);
                }
                if (source instanceof String) {
                    return support.convertStringToNumber((String) source, targetClass);
                }
                if (source instanceof Date) {
                    source = ((Date) source).getTime();
                }
            }
            if (recordType) {
                return FastBeanCopierSupport.copyToRecord(source, targetClass, support, Collections.emptySet());
            }
            if (beanLikeTarget && source instanceof Map) {
                return FastBeanCopierSupport.copy(source, support.beanFactory.newInstance(targetClass), support);
            }
            try {
                if (apacheConverter != null) {
                    return apacheConverter.convert(targetClass, source);
                }
                if (mapInterfaceType) {
                    if (source instanceof Map) {
                        return support.copyMap((Map<?, ?>) source);
                    }
                    if (source instanceof Collection) {
                        return support.convertCollectionToMap((Collection<?>) source, this);
                    }
                    ClassDescription sourceType = ClassDescriptions.getDescription(source.getClass());
                    return FastBeanCopierSupport.copy(source, Maps.newHashMapWithExpectedSize(sourceType.getFieldSize()));
                }
                return FastBeanCopierSupport.copy(source, support.beanFactory.newInstance(targetClass), support);
            } catch (Throwable e) {
                log.warn("Copy {} to {} failed", source, targetClass, e);
                throw e;
            }
        }
    }

    private static final class PlanKey {
        private final Class<?> targetClass;
        private final Class<?>[] genericTypes;

        private PlanKey(Class<?> targetClass, Class<?>[] genericTypes) {
            this.targetClass = targetClass;
            this.genericTypes = genericTypes;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PlanKey)) {
                return false;
            }
            PlanKey that = (PlanKey) obj;
            return targetClass == that.targetClass && Arrays.equals(genericTypes, that.genericTypes);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(targetClass) + Arrays.hashCode(genericTypes);
        }

        private boolean involves(ClassLoader loader) {
            if (FastBeanCopierSupport.isClassLoaderMatch(targetClass, loader)) {
                return true;
            }
            for (Class<?> genericType : genericTypes) {
                if (FastBeanCopierSupport.isClassLoaderMatch(genericType, loader)) {
                    return true;
                }
            }
            return false;
        }
    }
}
