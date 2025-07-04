package org.hswebframework.web.bean.accessor;

import org.objectweb.asm.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AsmBeanAccessor {

    private static final String PROPERTY_READER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyReader";
    private static final String PROPERTY_WRITER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyWriter";
    private static final String TYPE_CONVERTER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/TypeConverter";
    private static final String RESOLVABLE_TYPE_INTERNAL_NAME = "org/springframework/core/ResolvableType";
    
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final ConcurrentHashMap<String, PropertyReader> READER_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, PropertyWriter> WRITER_CACHE = new ConcurrentHashMap<>();
    
    private static final ClassLoader CLASS_LOADER = AsmBeanAccessor.class.getClassLoader();

    /**
     * 创建用于访问指定类型属性的读取器，通过生成如下代码:
     *  <pre>{@code
     *     PropertyReader{
     *
     *         Object apply(Object object){
     *             return ((MyEntity)object).getName();
     *         }
     *
     *     }
     *
     *  }</pre>
     * @param clazz 类型
     * @param name 属性名称
     * @return PropertyReader
     */
    public PropertyReader createReader(Class<?> clazz, String name) {
        try {
            PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
            if (descriptor == null || descriptor.getReadMethod() == null) {
                throw new IllegalArgumentException("Property '" + name + "' not found or not readable in class " + clazz.getName());
            }

            Method readMethod = descriptor.getReadMethod();
            String className = generateClassName("PropertyReader");

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{PROPERTY_READER_INTERNAL_NAME});

            // 默认构造函数
            MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();

            // apply方法
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            mv.visitCode();

            // 类型转换
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));

            // 调用getter方法
            mv.visitMethodInsn(
                    readMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(readMethod.getDeclaringClass()),
                    readMethod.getName(),
                    Type.getMethodDescriptor(readMethod),
                    readMethod.getDeclaringClass().isInterface()
            );

            // 处理返回值
            Class<?> returnType = readMethod.getReturnType();
            if (returnType.isPrimitive()) {
                boxPrimitive(mv, returnType);
            }

            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();

            cw.visitEnd();

            byte[] bytes = cw.toByteArray();
            Class<?> generatedClass = defineClass(className, bytes);
            return (PropertyReader) generatedClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create PropertyReader for " + clazz.getName() + "." + name, e);
        }
    }

    /**
     * 创建用于访问指定类型属性的写入器，通过生成如下代码:
     *  <pre>{@code
     *     PropertyWriter{
     *
     *         void accept(Object o, Object o2){
     *             ((MyEntity)o).setName( (String) o2 );
     *         }
     *
     *     }
     *
     *  }</pre>
     * @param clazz 类型
     * @param name 属性名称
     * @param typeConverter 类型转换器
     * @return PropertyWriter
     */
    public PropertyWriter createWriter(Class<?> clazz, String name, TypeConverter typeConverter) {
        try {
            PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
            if (descriptor == null || descriptor.getWriteMethod() == null) {
                throw new IllegalArgumentException("Property '" + name + "' not found or not writable in class " + clazz.getName());
            }

            Method writeMethod = descriptor.getWriteMethod();
            String className = generateClassName("PropertyWriter");

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{PROPERTY_WRITER_INTERNAL_NAME});

            // 添加TypeConverter字段
            FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";", null, null);
            fv.visitEnd();

            // 构造函数
            MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L" + TYPE_CONVERTER_INTERNAL_NAME + ";)V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitVarInsn(Opcodes.ALOAD, 1);
            constructor.visitFieldInsn(Opcodes.PUTFIELD, className, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";");
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(2, 2);
            constructor.visitEnd();

            // accept方法
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
            mv.visitCode();

            // 类型转换目标对象
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));

            // 处理参数值
            Class<?> paramType = writeMethod.getParameterTypes()[0];

            if (typeConverter != null) {
                // 使用TypeConverter进行类型转换
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, className, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";");
                mv.visitVarInsn(Opcodes.ALOAD, 2);

                // 创建ResolvableType
                if (paramType.isPrimitive()) {
                    // 对于基本类型，使用包装类型
                    Class<?> wrapperType = getWrapperType(paramType);
                    mv.visitLdcInsn(Type.getType(wrapperType));
                } else {
                    mv.visitLdcInsn(Type.getType(paramType));
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, RESOLVABLE_TYPE_INTERNAL_NAME, "forClass", "(Ljava/lang/Class;)L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";", false);

                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_CONVERTER_INTERNAL_NAME, "convert", "(Ljava/lang/Object;L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";)Ljava/lang/Object;", true);

                // 转换为目标类型
                if (paramType.isPrimitive()) {
                    unboxPrimitive(mv, paramType);
                } else {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
                }
            } else {
                // 直接类型转换
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                if (paramType.isPrimitive()) {
                    unboxPrimitive(mv, paramType);
                } else {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
                }
            }

            // 调用setter方法
            mv.visitMethodInsn(
                    writeMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(writeMethod.getDeclaringClass()),
                    writeMethod.getName(),
                    Type.getMethodDescriptor(writeMethod),
                    writeMethod.getDeclaringClass().isInterface()
            );

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();

            cw.visitEnd();

            byte[] bytes = cw.toByteArray();
            Class<?> generatedClass = defineClass(className, bytes);
            return (PropertyWriter) generatedClass.getDeclaredConstructor(TypeConverter.class).newInstance(typeConverter);

        } catch (Throwable e) {
            throw new RuntimeException("Failed to create PropertyWriter for " + clazz.getName() + "." + name, e);
        }
    }
    
    private PropertyDescriptor findPropertyDescriptor(Class<?> clazz, String name) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (name.equals(pd.getName())) {
                    return pd;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find property descriptor for " + clazz.getName() + "." + name, e);
        }
    }
    
    private String generateClassName(String prefix) {
        return "org/hswebframework/web/bean/accessor/" + prefix + "$" + COUNTER.incrementAndGet();
    }
    
    private Class<?> defineClass(String className, byte[] bytes) {
        try {
            // 使用 MethodHandles.Lookup 来定义类，这是 Java 17 推荐的方式
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            return lookup.defineClass(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to define class " + className, e);
        }
    }
    
    private void boxPrimitive(MethodVisitor mv, Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (primitiveType == byte.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (primitiveType == short.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (primitiveType == int.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (primitiveType == long.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (primitiveType == float.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (primitiveType == double.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        } else if (primitiveType == char.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        }
    }
    
    private void unboxPrimitive(MethodVisitor mv, Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        } else if (primitiveType == byte.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B", false);
        } else if (primitiveType == short.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
        } else if (primitiveType == int.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        } else if (primitiveType == long.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
        } else if (primitiveType == float.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
        } else if (primitiveType == double.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
        } else if (primitiveType == char.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
        }
    }
    
    private Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        }
        return primitiveType;
    }
}
