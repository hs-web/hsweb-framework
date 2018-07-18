package org.hswebframework.web.bean;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.time.DateFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hswebframework.web.bean.ToString.Feature.coverIgnoreProperty;
import static org.hswebframework.web.bean.ToString.Feature.disableNestProperty;
import static org.hswebframework.web.bean.ToString.Feature.nullPropertyToEmpty;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public class DefaultToStringOperator<T> implements ToStringOperator<T> {

    private PropertyDescriptor[] descriptors;

    private Set<String> defaultIgnoreProperties;

    private long defaultFeatures = ToString.DEFAULT_FEATURE;

    private Map<String, PropertyDescriptor> descriptorMap;

    private Map<String, BiFunction<Object, ConvertConfig, Object>> converts;

    private Function<Object, String> coverStringConvert = (o) -> coverString(String.valueOf(o), 50);

    private Function<Class, BiFunction<Object, ConvertConfig, Object>> simpleConvertBuilder = type -> {
        if (Date.class.isAssignableFrom(type)) {
            return (value, f) -> DateFormatter.toString(((Date) value), "yyyy-MM-dd HH:mm:ss");
        } else {
            return (value, f) -> value;
        }
    };

    Predicate<Class> simpleTypePredicate = ((Predicate<Class>) String.class::isAssignableFrom)
            .or(Class::isEnum)
            .or(Class::isPrimitive)
            .or(Date.class::isAssignableFrom)
            .or(Number.class::isAssignableFrom)
            .or(Boolean.class::isAssignableFrom);
    private Class<T> targetType;

    public DefaultToStringOperator(Class<T> targetType) {
        this.targetType = targetType;
        descriptors = BeanUtils.getPropertyDescriptors(targetType);
        init();
    }

    public static String coverString(String str, double percent) {
        if (str.length() == 1) {
            return "*";
        }

        if (percent > 1) {
            percent = percent / 100d;
        }
        percent = 1 - percent;
        long size = Math.round(str.length() * percent);

        long end = (str.length() - size / 2);

        long start = str.length() - end;
        start = start == 0 && percent > 0 ? 1 : start;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i >= start && i <= end - 1) {
                chars[i] = '*';
            }
        }
        return new String(chars);
    }

    @SuppressWarnings("all")
    protected void init() {
        converts = new HashMap<>();
        descriptorMap = Arrays.stream(descriptors).collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
        //获取类上的注解
        ToString.Ignore classIgnore = AnnotationUtils.getAnnotation(targetType, ToString.Ignore.class);
        ToString.Features features = AnnotationUtils.getAnnotation(targetType, ToString.Features.class);
        if (null != features && features.value().length > 0) {
            defaultFeatures = ToString.Feature.createFeatures(features.value());
        } else {
            defaultFeatures = ToString.DEFAULT_FEATURE;
        }
        defaultIgnoreProperties = classIgnore == null ?
                new HashSet<>(new java.util.HashSet<>())
                : new HashSet<>(Arrays.asList(classIgnore.value()));

        //是否打码
        boolean defaultCover = classIgnore != null && classIgnore.cover();

        for (PropertyDescriptor descriptor : descriptors) {
            if ("class".equals(descriptor.getName())) {
                continue;
            }
            Class propertyType = descriptor.getPropertyType();
            String propertyName = descriptor.getName();
            BiFunction<Object, ConvertConfig, Object> convert;
            ToString.Ignore propertyIgnore = null;
            long propertyFeature = 0;
            try {
                Field field = ReflectionUtils.findField(targetType, descriptor.getName());
                if (null == field) {
                    log.warn("无法获取字段{},该字段将不会被打码!", descriptor.getName());
                }
                propertyIgnore = field.getAnnotation(ToString.Ignore.class);
                features = AnnotationUtils.getAnnotation(field, ToString.Features.class);
                if (propertyIgnore != null) {
                    for (String val : propertyIgnore.value()) {
                        defaultIgnoreProperties.add(field.getName().concat(".").concat(val));
                    }
                }
                if (null != features && features.value().length > 0) {
                    propertyFeature = ToString.Feature.createFeatures(features.value());
                }
            } catch (Exception e) {
                log.warn("无法获取字段{},该字段将不会被打码!", descriptor.getName());
            }
            //是否设置了打码
            boolean cover = (propertyIgnore == null && defaultCover) || (propertyIgnore != null && propertyIgnore.cover());
            //是否注解了ignore
            boolean hide = propertyIgnore != null;

            long finalPropertyFeature = propertyFeature;

            if (simpleTypePredicate.test(propertyType)) {
                BiFunction<Object, ConvertConfig, Object> simpleConvert = simpleConvertBuilder.apply(propertyType);
                convert = (value, f) -> {
                    long feature = finalPropertyFeature == 0 ? f.features : finalPropertyFeature;

                    value = simpleConvert.apply(value, f);
                    if (hide || f.ignoreProperty.contains(propertyName)) {
                        if (ToString.Feature.hasFeature(feature, ToString.Feature.coverIgnoreProperty)) {
                            return coverStringConvert.apply(value);
                        } else {
                            return null;
                        }
                    }
                    return value;
                };

            } else {
                boolean toStringOverride = false;
                try {
                    toStringOverride = propertyType.getMethod("toString").getDeclaringClass() != Object.class;
                } catch (NoSuchMethodException ignore) {
                }
                boolean finalToStringOverride = toStringOverride;
                boolean justReturn = propertyType.isArray()
                        || Collection.class.isAssignableFrom(propertyType)
                        || Map.class.isAssignableFrom(propertyType);

                convert = (value, f) -> {
                    if (f.ignoreProperty.contains(propertyName)) {
                        return null;
                    }
                    long feature = finalPropertyFeature == 0 ? f.features : finalPropertyFeature;

                    boolean jsonFormat = ToString.Feature.hasFeature(feature, ToString.Feature.jsonFormat);
                    boolean propertyJsonFormat = ToString.Feature.hasFeature(finalPropertyFeature, ToString.Feature.jsonFormat);

                    if (ToString.Feature.hasFeature(f.features, disableNestProperty)) {
                        return null;
                    }
                    if (!jsonFormat && finalToStringOverride) {
                        return String.valueOf(value);
                    }

                    Set<String> newIgnoreProperty = f.ignoreProperty
                            .stream()
                            .filter(property -> property.startsWith(propertyName.concat(".")))
                            .map(property -> property.substring(propertyName.length() + 1))
                            .collect(Collectors.toSet());

                    if (justReturn) {
                        if (value instanceof Object[]) {
                            value = Arrays.asList(((Object[]) value));
                        }
                        if (value instanceof Map) {
                            value = convertMap(((Map) value), feature, newIgnoreProperty);
                        }
                        if (value instanceof Collection) {
                            value = ((Collection) value).stream()
                                    .map((val) -> {
                                        if (val instanceof Map) {
                                            return convertMap(((Map) val), feature, newIgnoreProperty);
                                        }
                                        if (simpleTypePredicate.test(val.getClass())) {
                                            return val;
                                        }
                                        ToStringOperator operator = ToString.getOperator(val.getClass());
                                        if (operator instanceof DefaultToStringOperator) {
                                            return ((DefaultToStringOperator) operator).toMap(val, feature, newIgnoreProperty);
                                        }
                                        return operator.toString(val, feature, newIgnoreProperty);
                                    }).collect(Collectors.toList());

                        }
                        if (value instanceof Map) {
                            value = convertMap(((Map) value), feature, newIgnoreProperty);
                        }
                        if (propertyJsonFormat) {
                            return JSON.toJSONString(value);
                        }
                        return value;
                    }

                    ToStringOperator operator = ToString.getOperator(value.getClass());
                    if (!propertyJsonFormat && operator instanceof DefaultToStringOperator) {
                        return ((DefaultToStringOperator) operator).toMap(value, feature, newIgnoreProperty);
                    } else {
                        return operator.toString(value, feature, newIgnoreProperty);
                    }
                };
            }
            converts.put(descriptor.getName(), convert);
        }
    }

    class ConvertConfig {
        long        features;
        Set<String> ignoreProperty;

    }

    protected Map<String, Object> convertMap(Map<String, Object> obj, long features, Set<String> ignoreProperty) {
        if (ignoreProperty.isEmpty()) {
            return obj;
        }
        boolean cover = ToString.Feature.hasFeature(features, coverIgnoreProperty);
        boolean isNullPropertyToEmpty = ToString.Feature.hasFeature(features, nullPropertyToEmpty);
        boolean isDisableNestProperty = ToString.Feature.hasFeature(features, disableNestProperty);

        Map<String, Object> newMap = new HashMap<>(obj);
        Set<String> ignore = new HashSet<>(ignoreProperty.size());
        ignore.addAll(defaultIgnoreProperties);

        for (Map.Entry<String, Object> entry : newMap.entrySet()) {
            Object value = entry.getValue();

            if (value == null) {
                if (isNullPropertyToEmpty) {
                    entry.setValue("");
                }
                continue;
            }
            Class type = value.getClass();
            if (simpleTypePredicate.test(type)) {
                value = simpleConvertBuilder.apply(type).apply(value, null);
                if (ignoreProperty.contains(entry.getKey())) {
                    if (cover) {
                        value = coverStringConvert.apply(value);
                    } else {
                        ignore.add(entry.getKey());
                    }
                    entry.setValue(value);
                }

            } else {
                if (isDisableNestProperty) {
                    ignore.add(entry.getKey());
                }
            }
        }
        ignore.forEach(newMap::remove);
        return newMap;
    }

    protected Map<String, Object> toMap(T target, long features, Set<String> ignoreProperty) {
        Map<String, Object> map = target instanceof Map ? ((Map) target) : FastBeanCopier.copy(target, new LinkedHashMap<>());

        Set<String> ignore = ignoreProperty == null || ignoreProperty.isEmpty() ? defaultIgnoreProperties : ignoreProperty;
        ConvertConfig convertConfig = new ConvertConfig();
        convertConfig.ignoreProperty = ignore;
        convertConfig.features = features == -1 ? defaultFeatures : features;
        Set<String> realIgnore = new HashSet<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                if (ToString.Feature.hasFeature(features, ToString.Feature.nullPropertyToEmpty)) {
                    boolean isSimpleType = false;
                    PropertyDescriptor propertyDescriptor = descriptorMap.get(entry.getKey());
                    Class propertyType = null;
                    if (propertyDescriptor != null) {
                        propertyType = propertyDescriptor.getPropertyType();
                        isSimpleType = simpleTypePredicate.test(propertyType);
                    }
                    if (isSimpleType || propertyType == null) {
                        entry.setValue("");
                    } else if (propertyType.isArray() || Collection.class.isAssignableFrom(propertyType)) {
                        entry.setValue(new java.util.ArrayList<>());
                    } else {
                        entry.setValue(new java.util.HashMap<>());
                    }
                }
                continue;
            }
            BiFunction<Object, ConvertConfig, Object> converter = converts.get(entry.getKey());
            if (null != converter) {
                entry.setValue(converter.apply(value, convertConfig));
            }
            if (entry.getValue() == null) {
                realIgnore.add(entry.getKey());
            }
        }
        realIgnore.forEach(map::remove);

        return map;
    }

    @Override
    public String toString(T target, long features, Set<String> ignoreProperty) {
        if (target == null) {
            return "";
        }
        if (features == -1) {
            features = defaultFeatures;
        }

        Map<String, Object> mapValue = toMap(target, features, ignoreProperty);
        if (ToString.Feature.hasFeature(features, ToString.Feature.jsonFormat)) {
            return JSON.toJSONString(mapValue);
        }
        boolean writeClassName = ToString.Feature.hasFeature(features, ToString.Feature.writeClassname);

        StringJoiner joiner = new StringJoiner(", ", (writeClassName ? target.getClass().getSimpleName() : "") + "{", "}");

        mapValue.forEach((key, value) -> joiner.add(key.concat("=").concat(String.valueOf(value))));

        return joiner.toString();
    }
}
