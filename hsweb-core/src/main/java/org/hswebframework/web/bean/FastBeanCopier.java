package org.hswebframework.web.bean;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.proxy.Proxy;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public final class FastBeanCopier {
    private static final Map<CacheKey, Copier> CACHE = new ConcurrentHashMap<>();

    private static final PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();

    private static final ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();

    private static final Map<Class<?>, Class<?>> wrapperClassMapping = new HashMap<>();

    @SuppressWarnings("all")
    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private static BeanFactory BEAN_FACTORY;

    public static final DefaultConverter DEFAULT_CONVERT;

    public static void setBeanFactory(BeanFactory beanFactory) {
        BEAN_FACTORY = beanFactory;
        DEFAULT_CONVERT.setBeanFactory(beanFactory);
    }

    public static BeanFactory getBeanFactory() {
        return BEAN_FACTORY;
    }

    static {
        wrapperClassMapping.put(byte.class, Byte.class);
        wrapperClassMapping.put(short.class, Short.class);
        wrapperClassMapping.put(int.class, Integer.class);
        wrapperClassMapping.put(float.class, Float.class);
        wrapperClassMapping.put(double.class, Double.class);
        wrapperClassMapping.put(char.class, Character.class);
        wrapperClassMapping.put(boolean.class, Boolean.class);
        wrapperClassMapping.put(long.class, Long.class);
        BEAN_FACTORY = new BeanFactory() {
            @Override
            @SneakyThrows
            @SuppressWarnings("all")
            public <T> T newInstance(Class<T> beanType) {
                return beanType == Map.class ? (T) new HashMap<>() : beanType.newInstance();
            }
        };
        DEFAULT_CONVERT = new DefaultConverter();
        DEFAULT_CONVERT.setBeanFactory(BEAN_FACTORY);
    }

    @SuppressWarnings("all")
    public static Set<String> include(String... inculdeProperties) {
        return new HashSet<String>(Arrays.asList(inculdeProperties)) {
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
        return copy(source, target.newInstance(), DEFAULT_CONVERT, ignore);
    }

    public static <T, S> T copy(S source, T target, Converter converter, String... ignore) {
        return copy(source, target, converter, (ignore == null || ignore.length == 0) ? Collections.emptySet() : new HashSet<>(Arrays.asList(ignore)));
    }

    public static <T, S> T copy(S source, T target, Set<String> ignore) {
        return copy(source, target, DEFAULT_CONVERT, ignore);
    }

    @SuppressWarnings("all")
    public static <T, S> T copy(S source, T target, Converter converter, Set<String> ignore) {
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
        if (autoCreate) {
            return CACHE.computeIfAbsent(key, k -> createCopier(k.sourceType, k.targetType));
        } else {
            return CACHE.get(key);
        }

    }

    private static CacheKey createCacheKey(Class<?> source, Class<?> target) {
        return new CacheKey(source, target);
    }

    public static Copier createCopier(Class<?> source, Class<?> target) {
        String sourceName = source.getName();
        String tartName = target.getName();
        if (sourceName.startsWith("package ")) {
            sourceName = sourceName.substring("package ".length());
        }
        if (tartName.startsWith("package ")) {
            tartName = tartName.substring("package ".length());
        }
        String method = "public void copy(Object s, Object t, java.util.Set ignore, " +
                "org.hswebframework.web.bean.Converter converter){\n" +
                "try{\n\t" +
                sourceName + " $$__source=(" + sourceName + ")s;\n\t" +
                tartName + " $$__target=(" + tartName + ")t;\n\t" +
                createCopierCode(source, target) +
                "}catch(Throwable e){\n" +
                "\tthrow e;" +
                "\n}\n" +
                "\n}";
        try {
            @SuppressWarnings("all")
            Proxy<Copier> proxy = Proxy
                    .create(Copier.class, new Class[]{source, target})
                    .addMethod(method);
            return proxy.newInstance();
        } catch (Exception e) {
            log.error("创建bean copy 代理对象失败:\n{}", method, e);
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    private static Map<String, ClassProperty> createProperty(Class<?> type) {

        List<String> fieldNames = Arrays.stream(type.getDeclaredFields())
                                        .map(Field::getName).collect(Collectors.toList());

        return Stream.of(propertyUtils.getPropertyDescriptors(type))
                     .filter(property -> !property
                             .getName()
                             .equals("class") && property.getReadMethod() != null && property.getWriteMethod() != null)
                     .map(BeanClassProperty::new)
                     //让字段有序
                     .sorted(Comparator.comparing(property -> fieldNames.indexOf(property.name)))
                     .collect(Collectors.toMap(ClassProperty::getName, Function.identity(), (k, k2) -> k, LinkedHashMap::new));

    }

    private static Map<String, ClassProperty> createMapProperty(Map<String, ClassProperty> template) {
        return template.values().stream().map(classProperty -> new MapClassProperty(classProperty.name))
                       .collect(Collectors.toMap(ClassProperty::getName, Function.identity(), (k, k2) -> k, LinkedHashMap::new));
    }

    private static String createCopierCode(Class<?> source, Class<?> target) {
        Map<String, ClassProperty> sourceProperties = null;

        Map<String, ClassProperty> targetProperties = null;

        //源类型为Map
        if (Map.class.isAssignableFrom(source)) {
            if (!Map.class.isAssignableFrom(target)) {
                targetProperties = createProperty(target);
                sourceProperties = createMapProperty(targetProperties);

            }
        } else if (Map.class.isAssignableFrom(target)) {
            if (!Map.class.isAssignableFrom(source)) {
                sourceProperties = createProperty(source);
                targetProperties = createMapProperty(sourceProperties);

            }
        } else {
            targetProperties = createProperty(target);
            sourceProperties = createProperty(source);
        }
        if (sourceProperties == null || targetProperties == null) {
            throw new UnsupportedOperationException("不支持的类型,source:" + source + " target:" + target);
        }
        StringBuilder code = new StringBuilder();

        for (ClassProperty sourceProperty : sourceProperties.values()) {
            ClassProperty targetProperty = targetProperties.get(sourceProperty.getName());
            if (targetProperty == null) {
                continue;
            }
            code.append("if(!ignore.contains(\"").append(sourceProperty.getName()).append("\")){\n\t");
            if (!sourceProperty.isPrimitive()) {
                code.append("if($$__source.").append(sourceProperty.getReadMethod()).append("!=null){\n");
            }
            code.append(targetProperty.generateVar(targetProperty.getName())).append("=")
                .append(sourceProperty.generateGetter(target, targetProperty.getType()))
                .append(";\n");

            if (!targetProperty.isPrimitive()) {
                code.append("\tif(").append(sourceProperty.getName()).append("!=null){\n");
            }
            code
                    .append("\t$$__target.")
                    .append(targetProperty.generateSetter(targetProperty.getType(), sourceProperty.getName()))
                    .append(";\n");
            if (!targetProperty.isPrimitive()) {
                code.append("\t}\n");
            }
            if (!sourceProperty.isPrimitive()) {
                code.append("\t}\n");
            }
            code.append("}\n");
        }
        return code.toString();
    }

    static abstract class ClassProperty {

        @Getter
        protected String name;

        @Getter
        protected String readMethodName;

        @Getter
        protected String writeMethodName;

        @Getter
        protected BiFunction<Class<?>, Class<?>, String> getter;

        @Getter
        protected BiFunction<Class<?>, String, String> setter;

        @Getter
        protected Class<?> type;

        @Getter
        protected Class<?> beanType;

        public String getReadMethod() {
            return readMethodName + "()";
        }

        public String generateVar(String name) {
            return getTypeName().concat(" ").concat(name);
        }

        public String getTypeName() {
            return getTypeName(type);
        }

        public String getTypeName(Class<?> type) {
            String targetTypeName = type.getName();
            if (type.isArray()) {
                targetTypeName = type.getComponentType().getName() + "[]";
            }
            return targetTypeName;
        }

        public boolean isPrimitive() {
            return isPrimitive(getType());
        }

        public boolean isPrimitive(Class<?> type) {
            return type.isPrimitive();
        }

        public boolean isWrapper() {
            return isWrapper(getType());
        }

        public boolean isWrapper(Class<?> type) {
            return wrapperClassMapping.containsValue(type);
        }

        protected Class<?> getPrimitiveType(Class<?> type) {
            return wrapperClassMapping.entrySet().stream()
                                      .filter(entry -> entry.getValue() == type)
                                      .map(Map.Entry::getKey)
                                      .findFirst()
                                      .orElse(null);
        }

        protected Class<?> getWrapperType() {
            return wrapperClassMapping.get(type);
        }

        protected String castWrapper(String getter) {
            return getWrapperType().getSimpleName().concat(".valueOf(").concat(getter).concat(")");
        }

        public BiFunction<Class<?>, Class<?>, String> createGetterFunction() {

            return (targetBeanType, targetType) -> {
                String getterCode = "$$__source." + getReadMethod();

                String generic = "org.hswebframework.web.bean.FastBeanCopier.EMPTY_CLASS_ARRAY";
                Field field = ReflectionUtils.findField(targetBeanType, name);
                boolean hasGeneric = false;
                if (field != null) {
                    String[] arr = Arrays.stream(ResolvableType.forField(field)
                                                               .getGenerics())
                                         .map(ResolvableType::getRawClass)
                                         .filter(Objects::nonNull)
                                         .map(t -> t.getName().concat(".class"))
                                         .toArray(String[]::new);
                    if (arr.length > 0) {
                        generic = "new Class[]{" + String.join(",", arr) + "}";
                        hasGeneric = true;
                    }
                }
                String convert = "converter.convert((Object)(" + (isPrimitive() ? castWrapper(getterCode) : getterCode) + "),"
                        + getTypeName(targetType) + ".class," + generic + ")";
                StringBuilder convertCode = new StringBuilder();

                if (targetType != getType()) {
                    if (isPrimitive(targetType)) {
                        boolean sourceIsWrapper = isWrapper();
                        Class<?> targetWrapperClass = wrapperClassMapping.get(targetType);

                        Class<?> sourcePrimitive = getPrimitiveType(getType());
                        //目标字段是基本数据类型,源字段是包装器类型
                        // source.getField().intValue();
                        if (sourceIsWrapper) {
                            convertCode
                                    .append(getterCode)
                                    .append(".")
                                    .append(sourcePrimitive.getName())
                                    .append("Value()");
                        } else {
                            //类型不一致，调用convert转换
                            convertCode.append("((").append(targetWrapperClass.getName())
                                       .append(")")
                                       .append(convert)
                                       .append(").")
                                       .append(targetType.getName())
                                       .append("Value()");
                        }

                    } else if (isPrimitive()) {
                        boolean targetIsWrapper = isWrapper(targetType);
                        //源字段类型为基本数据类型，目标字段为包装器类型
                        if (targetIsWrapper) {
                            convertCode.append(targetType.getName())
                                       .append(".valueOf(")
                                       .append(getterCode)
                                       .append(")");
                        } else {
                            convertCode.append("(").append(targetType.getName())
                                       .append(")(")
                                       .append(convert)
                                       .append(")");
                        }
                    } else {
                        convertCode.append("(").append(getTypeName(targetType))
                                   .append(")(")
                                   .append(convert)
                                   .append(")");
                    }
                } else {
                    if (Cloneable.class.isAssignableFrom(targetType)) {
                        try {
                            convertCode
                                    .append("(")
                                    .append(getTypeName())
                                    .append(")")
                                    .append(getterCode)
                                    .append(".clone()");
                        } catch (Exception e) {
                            convertCode.append(getterCode);
                        }
                    } else {
                        if ((Map.class.isAssignableFrom(targetType)
                                || Collection.class.isAssignableFrom(type)) && hasGeneric) {
                            convertCode.append("(").append(getTypeName()).append(")").append(convert);
                        } else {
                            convertCode.append("(").append(getTypeName()).append(")").append(getterCode);
//                            convertCode.append(getterCode);
                        }

                    }

                }
//                if (!isPrimitive()) {
//                    return getterCode + "!=null?" + convertCode.toString() + ":null";
//                }
                return convertCode.toString();
            };
        }

        public BiFunction<Class<?>, String, String> createSetterFunction(Function<String, String> settingNameSupplier) {
            return (sourceType, paramGetter) -> settingNameSupplier.apply(paramGetter);
        }

        public String generateGetter(Class<?> targetBeanType, Class<?> targetType) {
            return getGetter().apply(targetBeanType, targetType);
        }

        public String generateSetter(Class<?> targetType, String getter) {
            return getSetter().apply(targetType, getter);
        }
    }

    static class BeanClassProperty extends ClassProperty {
        public BeanClassProperty(PropertyDescriptor descriptor) {
            type = descriptor.getPropertyType();
            readMethodName = descriptor.getReadMethod().getName();
            writeMethodName = descriptor.getWriteMethod().getName();

            getter = createGetterFunction();
            setter = createSetterFunction(paramGetter -> writeMethodName + "(" + paramGetter + ")");
            name = descriptor.getName();
            beanType = descriptor.getReadMethod().getDeclaringClass();

        }
    }

    static class MapClassProperty extends ClassProperty {
        public MapClassProperty(String name) {
            type = Object.class;
            this.name = name;
            this.readMethodName = "get";
            this.writeMethodName = "put";

            this.getter = createGetterFunction();
            this.setter = createSetterFunction(paramGetter -> "put(\"" + name + "\"," + paramGetter + ")");
            beanType = Map.class;
        }

        @Override
        public String getReadMethod() {
            return "get(\"" + name + "\")";
        }

        @Override
        public String getReadMethodName() {
            return "get(\"" + name + "\")";
        }
    }


    public static final class DefaultConverter implements Converter {
        private BeanFactory beanFactory = BEAN_FACTORY;

        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        public Collection<?> newCollection(Class<?> targetClass) {

            if (targetClass == List.class) {
                return new ArrayList<>();
            } else if (targetClass == ConcurrentHashMap.KeySetView.class) {
                return ConcurrentHashMap.newKeySet();
            } else if (targetClass == Set.class) {
                return new HashSet<>();
            } else if (targetClass == Queue.class) {
                return new LinkedList<>();
            } else {
                try {
                    return (Collection<?>) targetClass.newInstance();
                } catch (Exception e) {
                    throw new UnsupportedOperationException("不支持的类型:" + targetClass, e);
                }
            }
        }

        @Override
        @SuppressWarnings("all")
        @SneakyThrows
        public <T> T convert(Object source, Class<T> targetClass, Class[] genericType) {
            if (source == null) {
                return null;
            }
            ClassDescription target = ClassDescriptions.getDescription(targetClass);

            if (target.isEnumType()) {
                if (source instanceof EnumDict) {
                    Object val = (T) ((EnumDict) source).getValue();
                    if (targetClass.isInstance(val)) {
                        return ((T) val);
                    }
                    return convert(val, targetClass, genericType);
                }
            }
            if (targetClass == String.class) {
                if (source instanceof Date) {
                    // TODO: 18-4-16 自定义格式
                    return (T) DateFormatter.toString(((Date) source), "yyyy-MM-dd HH:mm:ss");
                }
                return (T) String.valueOf(source);
            }
            if (targetClass == Object.class) {
                return (T) source;
            }
            if (targetClass == Date.class) {
                if (source instanceof String) {
                    T parsed = (T) DateFormatter.fromString((String) source);
                    if (parsed == null) {
                        return (T) converterByApache(Date.class, source);
                    }
                    return parsed;
                }
                if (source instanceof Number) {
                    return (T) new Date(((Number) source).longValue());
                }
                if (source instanceof Date) {
                    return (T) new Date(((Date) source).getTime());
                }
            }
            if (target.isCollectionType()) {
                Collection collection = newCollection(targetClass);
                Collection sourceCollection;
                if (source instanceof Collection) {
                    sourceCollection = (Collection) source;
                } else if (source instanceof Object[]) {
                    sourceCollection = Arrays.asList((Object[]) source);
                } else if (source instanceof Map) {
                    sourceCollection = ((Map<?, ?>) source).values();
                } else {
                    if (source instanceof String) {
                        String stringValue = ((String) source);
                        sourceCollection = Arrays.asList(stringValue.split("[,]"));
                    } else {
                        sourceCollection = Arrays.asList(source);
                    }
                }
                //转换泛型
                if (genericType != null && genericType.length > 0 && genericType[0] != Object.class) {
                    for (Object sourceObj : sourceCollection) {
                        collection.add(convert(sourceObj, genericType[0], null));
                    }
                } else {
                    collection.addAll(sourceCollection);
                }
                return (T) collection;
            }
            if (target.isEnumType()) {
                if (target.isEnumDict()) {
                    String strVal = String.valueOf(source);
                    Object val = null;
                    for (Object anEnum : target.getEnums()) {
                        EnumDict dic = ((EnumDict) anEnum);
                        Enum e = ((Enum<?>) anEnum);
                        if (dic.eq(source) || e.name().equalsIgnoreCase(strVal)) {
                            val = (T) anEnum;
                            break;
                        }
                    }
                    if (val == null) {
                        return null;
                    }
                    if (targetClass.isInstance(val)) {
                        return ((T) val);
                    }
                    return convert(val, targetClass, genericType);
                }
                String strSource = String.valueOf(source);
                for (Object e : target.getEnums()) {
                    Enum t = ((Enum<?>) e);
                    if ((t.name().equalsIgnoreCase(strSource)
                            || Objects.equals(String.valueOf(t.ordinal()), strSource))) {
                        return (T) e;
                    }
                }

                log.warn("无法将:{}转为枚举:{}", source, targetClass);
                return null;
            }
            //转换为数组
            if (target.isArrayType()) {
                Class<?> componentType = targetClass.getComponentType();
                List<?> val = convert(source, List.class, new Class[]{componentType});
                return (T) val.toArray((Object[]) Array.newInstance(componentType, val.size()));
            }
            if (target.isNumber()) {
                if (source instanceof String) {
                    return (T) NumberUtils.parseNumber(String.valueOf(source), (Class) targetClass);
                }
                if (source instanceof Date) {
                    source = ((Date) source).getTime();
                }
            }
            try {
                org.apache.commons.beanutils.Converter converter = convertUtils.lookup(targetClass);
                if (null != converter) {
                    return converter.convert(targetClass, source);
                }

                //快速复制map
                if (targetClass == Map.class) {
                    if (source instanceof Map) {
                        return (T) copyMap(((Map<?, ?>) source));
                    }
                    if (source instanceof Collection) {
                        Map<Object, Object> map = new LinkedHashMap<>();
                        int i = 0;
                        for (Object o : ((Collection<?>) source)) {
                            if (genericType.length >= 2) {
                                map.put(convert(i++, genericType[0], EMPTY_CLASS_ARRAY), convert(o, genericType[1], EMPTY_CLASS_ARRAY));
                            } else {
                                map.put(i++, o);
                            }
                        }
                        return (T) map;

                    }
                    ClassDescription sourType = ClassDescriptions.getDescription(source.getClass());
                    return (T) copy(source, Maps.newHashMapWithExpectedSize(sourType.getFieldSize()));
                }

                return copy(source, beanFactory.newInstance(targetClass), this);
            } catch (Exception e) {
                log.warn("复制类型{}->{}失败", targetClass, e);
                throw e;
            }
//            return null;
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

        private Object converterByApache(Class<?> targetClass, Object source) {
            org.apache.commons.beanutils.Converter converter = convertUtils.lookup(targetClass);
            if (null != converter) {
                return converter.convert(targetClass, source);
            }
            return null;
        }
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

        public int hashCode() {
            int result = this.targetType != null ? this.targetType.hashCode() : 0;
            result = 31 * result + (this.sourceType != null ? this.sourceType.hashCode() : 0);
            return result;
        }
    }
}
