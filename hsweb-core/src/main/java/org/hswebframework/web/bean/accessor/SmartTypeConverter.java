package org.hswebframework.web.bean.accessor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.bean.ClassDescription;
import org.hswebframework.web.bean.ClassDescriptions;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.utils.DynamicArrayList;
import org.springframework.core.ResolvableType;
import org.springframework.util.NumberUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能类型转换器，基于FastBeanCopier的DefaultConverter实现
 *
 * @author zhouhao
 * @since 5.0.1
 */
@Slf4j
public class SmartTypeConverter implements TypeConverter {

    static final DateFormatter FORMATTER = Objects.requireNonNull(
            DateFormatter.getFormatter("2025-07-05 10:49:32")
    );


    public static final SmartTypeConverter INSTANCE = new SmartTypeConverter();

    private static final ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();

    @Override
    public Object convert(Object source, ResolvableType type) {
        if (source == null) {
            return null;
        }

        Class<?> targetClass = type.toClass();
        if (targetClass == Object.class) {
            return source;
        }

        ClassDescription target = ClassDescriptions.getDescription(targetClass);

        // 处理枚举类型
        if (target.isEnumType()) {
            return convertToEnum(source, target, type);
        }

        // 处理基本类型转换
        if (targetClass == String.class) {
            return convertToString(source);
        }

        // 处理数字类型
        if (target.isNumber()) {
            return convertToNumber(source, targetClass);
        }

        // 处理Map类型
        if (target.isMapType()) {
            return convertToMap(source, type);
        }

        // 处理集合类型
        if (target.isCollectionType()) {
            return convertToCollection(source, type);
        }

        // 处理数组类型
        if (target.isArrayType()) {
            return convertToArray(source, targetClass);
        }

        if (targetClass == Date.class) {
            return convertToDate(source);
        }

        org.apache.commons.beanutils.Converter converter = convertUtils.lookup(targetClass);
        if (converter != null) {
            return converter.convert(targetClass, source);
        }
        // 处理Bean类型
        return convertToBean(source, targetClass);
    }

    /**
     * 转换为枚举类型
     */
    private Object convertToEnum(Object source, ClassDescription target, ResolvableType type) {
        // 处理源对象本身为EnumDict的情况
        if (source instanceof EnumDict) {
            Object val = ((EnumDict<?>) source).getValue();
            if (target.getType().isInstance(val)) {
                return val;
            }

            return convert(val, type);
        }

        String strVal = String.valueOf(source);
        Object result = null;

        // 如果是EnumDict类型
        if (target.isEnumDict()) {
            for (Object anEnum : target.getEnums()) {
                EnumDict<?> dic = (EnumDict<?>) anEnum;
                Enum<?> e = (Enum<?>) anEnum;
                if (dic.eq(source) || e.name().equalsIgnoreCase(strVal)) {
                    result = anEnum;
                    break;
                }
            }
        } else {
            // 普通枚举类型
            for (Object e : target.getEnums()) {
                Enum<?> enumValue = (Enum<?>) e;
                if (enumValue.name().equalsIgnoreCase(strVal) ||
                        Objects.equals(String.valueOf(enumValue.ordinal()), strVal)) {
                    result = e;
                    break;
                }
            }
        }

        if (result == null) {
            log.warn("can not converter: {} to:{}", source, type);
            return null;
        }

        if (type.toClass().isInstance(result)) {
            return result;
        }

        return convert(result,type);
    }

    /**
     * 转换为字符串类型
     */
    private String convertToString(Object source) {
        if (source instanceof Date date) {
            return FORMATTER.toString(date);
        }
        return String.valueOf(source);
    }

    /**
     * 转换为日期类型
     */
    private Date convertToDate(Object source) {
        if (source instanceof String str) {
            var parser = DateFormatter.getFormatter(str);
            if (parser != null) {
                return parser.format(str);
            }
            // 如果解析失败，尝试使用Apache Commons BeanUtils转换
            return convertByApache(Date.class, source);
        }

        if (source instanceof Number) {
            return new Date(((Number) source).longValue());
        }

        if (source instanceof Date) {
            return new Date(((Date) source).getTime());
        }

        return null;
    }

    /**
     * 转换为集合类型
     */
    private Collection<?> convertToCollection(Object source, ResolvableType type) {
        var collection = createNewCollection(type.toClass());
        var sourceCollection = toSourceCollection(source);
        var genericTypes = type.getGenerics();
        // 转换泛型元素
        if (genericTypes.length > 0 && genericTypes[0].toClass() != Object.class) {
            for (Object sourceObj : sourceCollection) {
                collection.add(convert(sourceObj, genericTypes[0]));
            }
        } else {
            collection.addAll(sourceCollection);
        }

        return collection;
    }

    /**
     * 创建新的集合实例
     */
    @SuppressWarnings("all")
    private Collection<Object> createNewCollection(Class<?> targetClass) {
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
                return (Collection<Object>) targetClass.getDeclaredConstructor().newInstance();
            } catch (Throwable e) {
                throw new UnsupportedOperationException("Unsupported Collection Type:" + targetClass, e);
            }
        }
    }

    /**
     * 将源对象转换为Collection
     */
    private Collection<?> toSourceCollection(Object source) {
        if (source instanceof Collection) {
            return (Collection<?>) source;
        } else if (source.getClass().isArray()) {
            return new DynamicArrayList<>(source);
        } else if (source instanceof Map) {
            return ((Map<?, ?>) source).values();
        } else if (source instanceof String str) {
            return Arrays.asList(str.split(","));
        } else {
            return List.of(source);
        }
    }

    /**
     * 转换为数组类型
     */
    private Object convertToArray(Object source, Class<?> targetClass) {
        var componentType = targetClass.getComponentType();
        var list = (List<?>) convert(source, ResolvableType.forClassWithGenerics(List.class, componentType));
        int size = list.size();

        var array = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

    /**
     * 转换为数字类型
     */
    private Object convertToNumber(Object source, Class<?> targetClass) {
        if (source instanceof String str) {
            return NumberUtils.parseNumber(str, (Class<? extends Number>) targetClass);
        }
        if (source instanceof Date) {
            source = ((Date) source).getTime();
        }
        return convertByApache(targetClass, source);
    }

    /**
     * 转换为Map类型
     */
    private Object convertToMap(Object source, ResolvableType type) {
        Class<?> targetClass = type.toClass();

        if (source instanceof Map) {
            return copyMap((Map<?, ?>) source, targetClass);
        }

        if (source instanceof Collection) {
            Map<Object, Object> map = createNewMap(targetClass);
            int i = 0;
            for (Object obj : (Collection<?>) source) {
                Object key = i++;
                Object value = obj;

                ResolvableType[] generics = type.getGenerics();
                if (generics.length >= 2) {
                    key = convert(key, generics[0]);
                    value = convert(value, generics[1]);
                }

                map.put(key, value);
            }
            return map;
        }

        // 将Bean转换为Map
        return FastBeanCopier.copy(source, createNewMap(targetClass));
    }

    /**
     * 创建新的Map实例
     */
    private Map<Object, Object> createNewMap(Class<?> targetClass) {
        if (targetClass == TreeMap.class) {
            return new TreeMap<>();
        }
        if (targetClass == LinkedHashMap.class) {
            return new LinkedHashMap<>();
        }
        if (targetClass == ConcurrentHashMap.class) {
            return new ConcurrentHashMap<>();
        }
        if (targetClass == Map.class) {
            return new HashMap<>();
        }

        // 尝试创建指定类型的Map
        try {
            return (Map<Object, Object>) targetClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            // 如果无法创建，返回默认的HashMap
            return new HashMap<>();
        }
    }

    /**
     * 复制Map
     */
    private Map<?, ?> copyMap(Map<?, ?> map, Class<?> targetClass) {
        Map<Object, Object> result = createNewMap(targetClass);
        result.putAll(map);
        return result;
    }

    /**
     * 转换为Bean类型
     */
    private Object convertToBean(Object source, Class<?> targetClass) {
        try {
            Object target = targetClass.getDeclaredConstructor().newInstance();
            return FastBeanCopier.copy(source, target);
        } catch (Throwable e) {
            log.warn("Copy {} to {} failed", source, targetClass, e);
            throw new RuntimeException("Convert to bean failed", e);
        }
    }

    /**
     * 使用Apache Commons BeanUtils进行转换
     */
    private <T> T convertByApache(Class<T> targetClass, Object source) {
        try {
            org.apache.commons.beanutils.Converter converter = convertUtils.lookup(targetClass);
            if (converter != null) {
                return converter.convert(targetClass, source);
            }
        } catch (Exception e) {
            log.warn("Apache converter failed for {} to {}", source, targetClass, e);
        }
        return null;
    }
}
