package org.hswebframework.web.bean.accessor;

import lombok.SneakyThrows;
import org.hswebframework.web.bean.Converter;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.core.ResolvableType;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 基于反射的Bean属性访问器
 * <p>
 * 智能选择访问策略：
 * <ul>
 * <li>如果有getter/setter方法，使用MethodHandle（高性能）</li>
 * <li>如果没有getter/setter方法，使用VarHandle直接访问字段</li>
 * </ul>
 *
 * @author AI Assistant
 */
public class ReflectionBeanAccessor implements PropertyAccessor {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * 创建属性读取器
     *
     * @param clazz 目标类
     * @param name  属性名称
     * @return PropertyReader
     */
    public PropertyReader createReader(Class<?> clazz, String name) {
        return createReaderInternal(clazz, name);
    }

    /**
     * 创建属性写入器
     *
     * @param clazz         目标类
     * @param name          属性名称
     * @param typeConverter 类型转换器
     * @return PropertyWriter
     */
    public PropertyWriter createWriter(Class<?> clazz, String name, TypeConverter typeConverter) {
        return createWriterInternal(clazz, name, typeConverter);
    }

    @Override
    public PropertyTransfer createTransfer(Class<?> sourceClass,
                                           String sourceName,
                                           boolean sourcePrimitive,
                                           Class<?> targetClass,
                                           String targetName) {
        try {
            PropertyDescriptor sourceDescriptor = findPropertyDescriptor(sourceClass, sourceName);
            PropertyDescriptor targetDescriptor = findPropertyDescriptor(targetClass, targetName);
            if (sourceDescriptor == null
                || sourceDescriptor.getReadMethod() == null
                || targetDescriptor == null
                || targetDescriptor.getWriteMethod() == null) {
                return null;
            }
            MethodHandle getter = LOOKUP.unreflect(sourceDescriptor.getReadMethod());
            MethodHandle setter = LOOKUP.unreflect(targetDescriptor.getWriteMethod());
            return new MethodHandlePropertyTransfer(getter, setter, sourcePrimitive);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ConverterPropertyTransfer createConverterTransfer(Class<?> sourceClass,
                                                             String sourceName,
                                                             boolean sourcePrimitive,
                                                             Class<?> targetClass,
                                                             String targetName,
                                                             boolean allowDirectAssignment) {
        try {
            PropertyDescriptor sourceDescriptor = findPropertyDescriptor(sourceClass, sourceName);
            PropertyDescriptor targetDescriptor = findPropertyDescriptor(targetClass, targetName);
            if (sourceDescriptor == null
                || sourceDescriptor.getReadMethod() == null
                || targetDescriptor == null
                || targetDescriptor.getWriteMethod() == null) {
                return null;
            }
            Method setter = targetDescriptor.getWriteMethod();
            MethodHandle getter = LOOKUP.unreflect(sourceDescriptor.getReadMethod());
            MethodHandle writer = LOOKUP.unreflect(setter);
            ResolvableType targetType = ResolvableType.forMethodParameter(setter, 0, targetClass);
            Class<?>[] genericTypes = resolveGenericTypes(targetType);
            return new MethodHandleConverterPropertyTransfer(getter,
                                                             writer,
                                                             sourcePrimitive,
                                                             targetType.toClass(),
                                                             genericTypes,
                                                             allowDirectAssignment);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 内部方法：创建PropertyReader实例
     */
    private PropertyReader createReaderInternal(Class<?> clazz, String name) {
        try {
            // 1. 首先尝试查找getter方法
            PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
            if (descriptor != null && descriptor.getReadMethod() != null) {
                // 使用MethodHandle访问getter方法
                Method getter = descriptor.getReadMethod();
                MethodHandle methodHandle = LOOKUP.unreflect(getter);
                return new MethodHandlePropertyReader(methodHandle);
            }

            // 2. 如果没有getter方法，尝试使用VarHandle直接访问字段
            Field field = findField(clazz, name);
            if (field != null) {
                field.setAccessible(true);
                try {
                    // 尝试使用提升权限的lookup访问私有字段
                    MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
                    VarHandle varHandle = privateLookup.unreflectVarHandle(field);
                    return new VarHandlePropertyReader(varHandle);
                } catch (Exception e) {
                    // 如果VarHandle访问失败，回退到传统反射
                    return new ReflectionFieldPropertyReader(field);
                }
            }

            throw new IllegalArgumentException("Property '" + name + "' not found or not readable in class " + clazz.getName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to create PropertyReader for " + clazz.getName() + "." + name, e);
        }
    }

    /**
     * 内部方法：创建PropertyWriter实例
     */
    private PropertyWriter createWriterInternal(Class<?> clazz, String name, TypeConverter typeConverter) {
        try {
            // 1. 首先尝试查找setter方法
            PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
            if (descriptor != null && descriptor.getWriteMethod() != null) {
                // 使用MethodHandle访问setter方法
                Method setter = descriptor.getWriteMethod();
                MethodHandle methodHandle = LOOKUP.unreflect(setter);
                return new MethodHandlePropertyWriter(methodHandle,
                        ResolvableType.forMethodParameter(setter, 0, clazz),
                        typeConverter);
            }

            // 2. 如果没有setter方法，尝试使用VarHandle直接访问字段
            Field field = findField(clazz, name);
            if (field != null) {
                field.setAccessible(true);
                try {
                    // 尝试使用提升权限的lookup访问私有字段
                    MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
                    VarHandle varHandle = privateLookup.unreflectVarHandle(field);
                    return new VarHandlePropertyWriter(varHandle, field, typeConverter);
                } catch (Exception e) {
                    // 如果VarHandle访问失败，回退到传统反射
                    return new ReflectionFieldPropertyWriter(field, typeConverter);
                }
            }

            throw new IllegalArgumentException("Property '" + name + "' not found or not writable in class " + clazz.getName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to create PropertyWriter for " + clazz.getName() + "." + name, e);
        }
    }

    /**
     * 查找属性描述符
     */
    private PropertyDescriptor findPropertyDescriptor(Class<?> clazz, String name) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                if (name.equals(descriptor.getName())) {
                    return descriptor;
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }

    /**
     * 查找字段
     */
    private Field findField(Class<?> clazz, String name) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 基于MethodHandle的PropertyReader实现
     */
    private record MethodHandlePropertyReader(MethodHandle methodHandle) implements PropertyReader {

        @Override
        public Object apply(Object target) {
            try {
                return methodHandle.invoke(target);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to read property", e);
            }
        }
    }

    /**
     * 基于VarHandle的PropertyReader实现
     */
    private record VarHandlePropertyReader(VarHandle varHandle) implements PropertyReader {

        @Override
        public Object apply(Object target) {
            try {
                return varHandle.get(target);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read property", e);
            }
        }
    }

    private static class MethodHandlePropertyTransfer implements PropertyTransfer {
        private final MethodHandle reader;
        private final MethodHandle writer;
        private final boolean sourcePrimitive;

        private MethodHandlePropertyTransfer(MethodHandle reader, MethodHandle writer, boolean sourcePrimitive) {
            this.reader = reader;
            this.writer = writer;
            this.sourcePrimitive = sourcePrimitive;
        }

        @Override
        @SneakyThrows
        public void transfer(Object source, Object target) {
            Object value = reader.invoke(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            writer.invoke(target, value);
        }
    }

    private static class MethodHandleConverterPropertyTransfer implements ConverterPropertyTransfer {
        private final MethodHandle reader;
        private final MethodHandle writer;
        private final boolean sourcePrimitive;
        private final boolean targetPrimitive;
        private final boolean allowDirectAssignment;
        private final Class<?> targetType;
        private final Class<?>[] genericTypes;

        private MethodHandleConverterPropertyTransfer(MethodHandle reader,
                                                     MethodHandle writer,
                                                     boolean sourcePrimitive,
                                                     Class<?> targetType,
                                                     Class<?>[] genericTypes,
                                                     boolean allowDirectAssignment) {
            this.reader = reader;
            this.writer = writer;
            this.sourcePrimitive = sourcePrimitive;
            this.targetPrimitive = targetType.isPrimitive();
            this.allowDirectAssignment = allowDirectAssignment;
            this.targetType = targetType;
            this.genericTypes = genericTypes;
        }

        @Override
        @SneakyThrows
        public void transfer(Object source, Object target, Converter converter) {
            Object value = reader.invoke(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            if (value instanceof EnumDict && isNumberType(targetType)) {
                Object enumValue = ((EnumDict<?>) value).getValue();
                if (enumValue != null) {
                    value = enumValue;
                }
            }
            if (!allowDirectAssignment || !isDirectAssignable(value)) {
                value = converter.convert(value, targetType, genericTypes);
            }
            if (value == null && !targetPrimitive) {
                return;
            }
            writer.invoke(target, value);
        }

        private boolean isDirectAssignable(Object value) {
            if (value == null) {
                return false;
            }
            if (targetPrimitive) {
                return resolveWrapperType(targetType).isInstance(value);
            }
            return targetType.isInstance(value);
        }
    }

    private static boolean isNumberType(Class<?> targetType) {
        return Number.class.isAssignableFrom(targetType)
            || (targetType.isPrimitive() && targetType != boolean.class && targetType != char.class);
    }

    private static Class<?>[] resolveGenericTypes(ResolvableType type) {
        Class<?>[] arr = java.util.Arrays.stream(type.getGenerics())
            .map(ResolvableType::getRawClass)
            .filter(java.util.Objects::nonNull)
            .toArray(Class[]::new);
        return arr.length == 0 ? org.hswebframework.web.bean.FastBeanCopierSupport.EMPTY_CLASS_ARRAY : arr;
    }

    /**
     * 基于MethodHandle的PropertyWriter实现
     */
    private static class MethodHandlePropertyWriter implements PropertyWriter {
        private final MethodHandle methodHandle;
        private final Class<?> paramType;
        private final TypeConverter typeConverter;
        private final ResolvableType resolvableType;

        public MethodHandlePropertyWriter(MethodHandle methodHandle, ResolvableType type, TypeConverter typeConverter) {
            this.methodHandle = methodHandle;
            this.paramType = type.toClass();
            this.resolvableType = type;
            this.typeConverter = typeConverter;
        }

        @Override
        @SneakyThrows
        public void accept(Object target, Object value) {

            methodHandle.invoke(target, convertValue(value));

        }

        private Object convertValue(Object value) {
            if (value == null) {
                return value;
            }
            if (paramType.isPrimitive()) {
                Class<?> wrapperType = resolveWrapperType(paramType);
                if (wrapperType.isInstance(value)) {
                    return value;
                }
            } else if (paramType.isInstance(value)) {
                return value;
            }
            if (typeConverter == null) {
                return value;
            }
            return typeConverter.convert(value, resolvableType);
        }

    }

    /**
     * 基于VarHandle的PropertyWriter实现
     */
    private static class VarHandlePropertyWriter implements PropertyWriter {
        private final VarHandle varHandle;
        private final Field field;
        private final ResolvableType resolvableType;
        private final TypeConverter typeConverter;

        public VarHandlePropertyWriter(VarHandle varHandle, Field field, TypeConverter typeConverter) {
            this.varHandle = varHandle;
            this.field = field;
            this.typeConverter = typeConverter;
            this.resolvableType = ResolvableType.forField(field);
        }

        @Override
        public void accept(Object target, Object value) {
            try {
                Object convertedValue = convertValue(value);
                varHandle.set(target, convertedValue);
            } catch (Exception e) {
                throw new RuntimeException("Failed to write property", e);
            }
        }

        private Object convertValue(Object value) {
            if (value == null) {
                return value;
            }
            if (field.getType().isPrimitive()) {
                Class<?> wrapperType = resolveWrapperType(field.getType());
                if (wrapperType.isInstance(value)) {
                    return value;
                }
            } else if (field.getType().isInstance(value)) {
                return value;
            }
            if (typeConverter == null) {
                return value;
            }
            return typeConverter.convert(value, resolvableType);
        }

    }

    /**
     * 基于传统反射的PropertyReader实现（VarHandle的后备方案）
     */
    private static class ReflectionFieldPropertyReader implements PropertyReader {
        private final Field field;

        public ReflectionFieldPropertyReader(Field field) {
            this.field = field;
        }

        @Override
        public Object apply(Object target) {
            try {
                return field.get(target);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read field", e);
            }
        }
    }

    /**
     * 基于传统反射的PropertyWriter实现（VarHandle的后备方案）
     */
    private static class ReflectionFieldPropertyWriter implements PropertyWriter {
        private final Field field;
        private final ResolvableType fieldType;
        private final TypeConverter typeConverter;

        public ReflectionFieldPropertyWriter(Field field, TypeConverter typeConverter) {
            this.field = field;
            this.fieldType = ResolvableType.forField(field);
            this.typeConverter = typeConverter;
        }

        @Override
        @SneakyThrows
        public void accept(Object target, Object value) {

            Object convertedValue = convertValue(value);
            field.set(target, convertedValue);

        }

        private Object convertValue(Object value) {
            if (value == null) {
                return null;
            }
            if (field.getType().isPrimitive()) {
                Class<?> wrapperType = resolveWrapperType(field.getType());
                if (wrapperType.isInstance(value)) {
                    return value;
                }
            } else if (field.getType().isInstance(value)) {
                return value;
            }
            if (typeConverter == null) {
                return value;
            }
            return typeConverter.convert(value, fieldType);
        }

    }

    private static Class<?> resolveWrapperType(Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return Boolean.class;
        }
        if (primitiveType == byte.class) {
            return Byte.class;
        }
        if (primitiveType == short.class) {
            return Short.class;
        }
        if (primitiveType == int.class) {
            return Integer.class;
        }
        if (primitiveType == long.class) {
            return Long.class;
        }
        if (primitiveType == float.class) {
            return Float.class;
        }
        if (primitiveType == double.class) {
            return Double.class;
        }
        if (primitiveType == char.class) {
            return Character.class;
        }
        return primitiveType;
    }
}
