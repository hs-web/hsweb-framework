package org.hswebframework.web.bean;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.Extendable;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
final class ReflectFastBeanCopierBackend implements FastBeanCopierBackend {

    @Override
    public Copier createCopier(Class<?> source, Class<?> target) {
        try {
            Copier copier = new ReflectCopier(source, target);
            boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
            boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
            boolean targetIsMap = Map.class.isAssignableFrom(target);
            boolean sourceIsMap = Map.class.isAssignableFrom(source);
            if (sourceIsExtendable && targetIsMap) {
                copier = new ExtendableToMapCopier(copier);
            } else if (sourceIsMap && targetIsExtendable) {
                copier = new MapToExtendableCopier(copier);
            } else if (sourceIsExtendable) {
                copier = new ExtendableToBeanCopier(copier);
            }
            return copier;
        } catch (Throwable e) {
            log.error("create bean copy {} => {} failed", source, target, e);
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    static final class ReflectCopier implements Copier {
        private final List<PropertyMapping> mappings;
        private final List<ExtensionMapping> extensionMappings;

        ReflectCopier(Class<?> source, Class<?> target) {
            Map<String, FastBeanCopierPropertySupport.ClassProperty> sourceProperties = null;
            Map<String, FastBeanCopierPropertySupport.ClassProperty> targetProperties = null;

            boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
            boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
            boolean targetIsMap = Map.class.isAssignableFrom(target);
            boolean sourceIsMap = Map.class.isAssignableFrom(source);
            if (sourceIsMap) {
                if (!targetIsMap) {
                    targetProperties = FastBeanCopierPropertySupport.createProperty(target);
                    sourceProperties = FastBeanCopierPropertySupport.createMapProperty(targetProperties);
                }
            } else if (targetIsMap) {
                sourceProperties = FastBeanCopierPropertySupport.createProperty(source);
                targetProperties = FastBeanCopierPropertySupport.createMapProperty(sourceProperties);
            } else {
                targetProperties = FastBeanCopierPropertySupport.createProperty(target);
                sourceProperties = FastBeanCopierPropertySupport.createProperty(source);
            }
            if (sourceProperties == null || targetProperties == null) {
                throw new UnsupportedOperationException("不支持的类型,source:" + source + " target:" + target);
            }

            List<PropertyMapping> mappings = new ArrayList<>();
            List<ExtensionMapping> extensionMappings = new ArrayList<>();
            for (FastBeanCopierPropertySupport.ClassProperty sourceProperty : sourceProperties.values()) {
                FastBeanCopierPropertySupport.ClassProperty targetProperty = targetProperties.get(sourceProperty.getName());
                if (targetProperty == null) {
                    if (targetIsExtendable && !sourceIsExtendable && !sourceIsMap) {
                        extensionMappings.add(new ExtensionMapping(sourceProperty));
                    }
                    continue;
                }
                mappings.add(PropertyMapping.of(target, sourceProperty, targetProperty));
            }
            this.mappings = mappings;
            this.extensionMappings = extensionMappings;
        }

        @Override
        public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
            try {
                for (PropertyMapping mapping : mappings) {
                    mapping.copy(source, target, ignore, converter);
                }
                if (!extensionMappings.isEmpty()) {
                    Extendable extendable = (Extendable) target;
                    for (ExtensionMapping mapping : extensionMappings) {
                        mapping.copy(source, extendable, ignore);
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new UnsupportedOperationException(e.getMessage(), e);
            }
        }
    }

    static final class PropertyMapping {
        private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();

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

        private final String name;
        private final ValueReader reader;
        private final ValueWriter writer;
        private final Class<?> sourceType;
        private final Class<?> targetType;
        private final boolean sourcePrimitive;
        private final boolean targetPrimitive;
        private final Class<?>[] genericTypes;

        private PropertyMapping(String name,
                                ValueReader reader,
                                ValueWriter writer,
                                Class<?> sourceType,
                                Class<?> targetType,
                                boolean sourcePrimitive,
                                boolean targetPrimitive,
                                Class<?>[] genericTypes) {
            this.name = name;
            this.reader = reader;
            this.writer = writer;
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.sourcePrimitive = sourcePrimitive;
            this.targetPrimitive = targetPrimitive;
            this.genericTypes = genericTypes;
        }

        static PropertyMapping of(Class<?> target,
                                  FastBeanCopierPropertySupport.ClassProperty sourceProperty,
                                  FastBeanCopierPropertySupport.ClassProperty targetProperty) {
            return new PropertyMapping(sourceProperty.getName(),
                                       createReader(sourceProperty),
                                       createWriter(targetProperty),
                                       sourceProperty.getType(),
                                       targetProperty.getType(),
                                       sourceProperty.isPrimitive(),
                                       targetProperty.isPrimitive(),
                                       resolveGenericTypes(target, targetProperty));
        }

        void copy(Object source, Object target, Set<String> ignore, Converter converter) throws Throwable {
            if (ignore.contains(name)) {
                return;
            }
            Object value = reader.read(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            Object converted = convertValue(value, converter);
            if (converted == null && !targetPrimitive) {
                return;
            }
            writer.write(target, converted);
        }

        private Object convertValue(Object value, Converter converter) {
            if (value == null) {
                return null;
            }
            value = unwrapEnumDictValue(value);
            if (sourceType == targetType) {
                if (Cloneable.class.isAssignableFrom(targetType)) {
                    Object cloned = tryClone(value);
                    if (cloned != null) {
                        return cloned;
                    }
                }
                if ((Map.class.isAssignableFrom(targetType) || Collection.class.isAssignableFrom(targetType))
                    && genericTypes.length > 0) {
                    return converter.convert(value, (Class) targetType, genericTypes);
                }
                return value;
            }
            if (targetType == Object.class) {
                return value;
            }
            if ((Map.class.isAssignableFrom(targetType) || Collection.class.isAssignableFrom(targetType))
                && genericTypes.length > 0) {
                return converter.convert(value, (Class) targetType, genericTypes);
            }
            if (isDirectAssignable(targetType, value)) {
                return value;
            }
            return converter.convert(value, (Class) targetType, genericTypes);
        }

        private Object unwrapEnumDictValue(Object value) {
            if (!(value instanceof EnumDict)) {
                return value;
            }
            if (targetType.isEnum()) {
                return value;
            }
            if (isNumberType(targetType)) {
                Object enumValue = ((EnumDict<?>) value).getValue();
                return enumValue == null ? value : enumValue;
            }
            return value;
        }

        private static boolean isDirectAssignable(Class<?> targetType, Object value) {
            if (value == null) {
                return false;
            }
            if (targetType.isInstance(value)) {
                return true;
            }
            if (targetType.isPrimitive()) {
                Class<?> wrapper = PRIMITIVE_WRAPPERS.get(targetType);
                return wrapper != null && wrapper.isInstance(value);
            }
            return false;
        }

        private static boolean isNumberType(Class<?> targetType) {
            if (Number.class.isAssignableFrom(targetType)) {
                return true;
            }
            return targetType.isPrimitive()
                && targetType != boolean.class
                && targetType != char.class;
        }

        private static Object tryClone(Object value) {
            try {
                if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    Object clone = Array.newInstance(value.getClass().getComponentType(), length);
                    System.arraycopy(value, 0, clone, 0, length);
                    return clone;
                }
                Method clone = value.getClass().getMethod("clone");
                ReflectionUtils.makeAccessible(clone);
                return clone.invoke(value);
            } catch (Throwable ignore) {
                return null;
            }
        }

        static ValueReader createReader(FastBeanCopierPropertySupport.ClassProperty property) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return source -> ((Map<?, ?>) source).get(property.getName());
            }
            Method method = ReflectionUtils.findMethod(property.getBeanType(), property.getReadMethodName());
            if (method == null) {
                throw new IllegalStateException("Read method not found: " + property.getBeanType() + "." + property.getReadMethodName());
            }
            ReflectionUtils.makeAccessible(method);
            return new MethodValueReader(method);
        }

        static ValueWriter createWriter(FastBeanCopierPropertySupport.ClassProperty property) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return (target, value) -> ((Map<String, Object>) target).put(property.getName(), value);
            }
            Method method = ReflectionUtils.findMethod(property.getBeanType(), property.getWriteMethodName(), property.getType());
            if (method == null) {
                throw new IllegalStateException("Write method not found: " + property.getBeanType() + "." + property.getWriteMethodName());
            }
            ReflectionUtils.makeAccessible(method);
            return new MethodValueWriter(method);
        }

        private static Class<?>[] resolveGenericTypes(Class<?> target,
                                                      FastBeanCopierPropertySupport.ClassProperty property) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return FastBeanCopierSupport.EMPTY_CLASS_ARRAY;
            }
            Field field = ReflectionUtils.findField(target, property.getName());
            if (field == null) {
                field = ReflectionUtils.findField(property.getBeanType(), property.getName());
            }
            if (field == null) {
                return FastBeanCopierSupport.EMPTY_CLASS_ARRAY;
            }
            Class<?>[] arr = Arrays.stream(ResolvableType.forField(field, target).getGenerics())
                .map(ResolvableType::resolve)
                .filter(Objects::nonNull)
                .toArray(Class[]::new);
            return arr.length == 0 ? FastBeanCopierSupport.EMPTY_CLASS_ARRAY : arr;
        }
    }

    static final class ExtensionMapping {
        private final String name;
        private final boolean primitive;
        private final ValueReader reader;

        ExtensionMapping(FastBeanCopierPropertySupport.ClassProperty property) {
            this.name = property.getName();
            this.primitive = property.isPrimitive();
            this.reader = PropertyMapping.createReader(property);
        }

        void copy(Object source, Extendable target, Set<String> ignore) throws Throwable {
            if (ignore.contains(name)) {
                return;
            }
            Object value = reader.read(source);
            if (value == null && !primitive) {
                return;
            }
            target.setExtension(name, value);
        }
    }

    interface ValueReader {
        Object read(Object source) throws Throwable;
    }

    interface ValueWriter {
        void write(Object target, Object value) throws Throwable;
    }

    static final class MethodValueReader implements ValueReader {
        private final Method method;

        MethodValueReader(Method method) {
            this.method = method;
        }

        @Override
        @SneakyThrows
        public Object read(Object source) {
            return method.invoke(source);
        }
    }

    static final class MethodValueWriter implements ValueWriter {
        private final Method method;

        MethodValueWriter(Method method) {
            this.method = method;
        }

        @Override
        @SneakyThrows
        public void write(Object target, Object value) {
            method.invoke(target, value);
        }
    }
}
