package org.hswebframework.web.bean.accessor;

import lombok.SneakyThrows;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.ResolvableType;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

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
public class ReflectionBeanAccessor {

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
            if (value == null || paramType.isInstance(value)) {
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
            if (value == null || field.getType().isInstance(value)) {
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
            if (value == null || field.getType().isInstance(value)) {
                return null;
            }
            return typeConverter.convert(value, fieldType);
        }

    }
} 