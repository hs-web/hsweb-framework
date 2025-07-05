package org.hswebframework.web.bean.accessor;

import lombok.SneakyThrows;
import org.objectweb.asm.*;
import org.springframework.core.ResolvableType;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class AsmBeanAccessor {

    private static final String PROPERTY_READER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyReader";
    private static final String PROPERTY_WRITER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyWriter";
    private static final String TYPE_CONVERTER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/TypeConverter";
    private static final String RESOLVABLE_TYPE_INTERNAL_NAME = "org/springframework/core/ResolvableType";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @SneakyThrows
    public byte[] createReaderCode(Class<?> clazz, String name) {
        PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
        if (descriptor == null || descriptor.getReadMethod() == null) {
            throw new IllegalArgumentException("Property '" + name + "' not found or not readable in class " + clazz.getName());
        }

        Method readMethod = descriptor.getReadMethod();
        String className = generateClassName(clazz.getSimpleName() + "$" + name + "$Reader");

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

        return cw.toByteArray();
    }

    /**
     * 创建用于访问指定类型属性的读取器，通过生成如下代码:
     * <pre>{@code
     *     PropertyReader{
     *
     *         Object apply(Object object){
     *             return ((MyEntity)object).getName();
     *         }
     *
     *     }
     *
     *  }</pre>
     *
     * @param clazz 类型
     * @param name  属性名称
     * @return PropertyReader
     */
    public PropertyReader createReader(Class<?> clazz, String name) {
        try {

            Class<?> generatedClass = defineClass(createReaderCode(clazz, name));
            return (PropertyReader) generatedClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create PropertyReader for " + clazz.getName() + "." + name, e);
        }
    }

    public byte[] createWriterCode(Class<?> clazz, String className, PropertyDescriptor descriptor, TypeConverter converter) {
        if (descriptor == null || descriptor.getWriteMethod() == null) {
            throw new IllegalArgumentException("Property '" + descriptor + "' not found or not writable in class " + clazz.getName());
        }

        Method writeMethod = descriptor.getWriteMethod();
        className = className.replace(".", "/");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{PROPERTY_WRITER_INTERNAL_NAME});

        // 添加TypeConverter字段
        FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";", null, null);
        fv.visitEnd();

        // 处理参数值类型
        Class<?> paramType = writeMethod.getParameterTypes()[0];

        // 预先计算ResolvableType并添加为字段
        String resolvableTypeField = null;
        if (converter != null) {
            resolvableTypeField = "resolvableType";
            FieldVisitor resolvableTypeFv = cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, resolvableTypeField, "L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";", null, null);
            resolvableTypeFv.visitEnd();
        }

        // 构造函数 - 修改为直接接受TypeConverter和ResolvableType参数
        String constructorDescriptor = converter != null ? 
            "(L" + TYPE_CONVERTER_INTERNAL_NAME + ";L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";)V" :
            "(L" + TYPE_CONVERTER_INTERNAL_NAME + ";)V";
            
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructorDescriptor, null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        
        // 设置typeConverter字段
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitVarInsn(Opcodes.ALOAD, 1);
        constructor.visitFieldInsn(Opcodes.PUTFIELD, className, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";");

        // 设置ResolvableType字段（直接从构造参数获取）
        if (resolvableTypeField != null) {
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitVarInsn(Opcodes.ALOAD, 2); // ResolvableType参数
            constructor.visitFieldInsn(Opcodes.PUTFIELD, className, resolvableTypeField, "L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";");
        }

        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(2, 3);
        constructor.visitEnd();

        // accept方法
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();

        // 类型转换目标对象
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));

        if (converter != null) {
            // 生成一个优化的类型转换器，调用辅助方法来处理类型检查
            // 先生成辅助方法
            generateOptimizedConvertMethod(cw, className, paramType, resolvableTypeField);

            // 在accept方法中调用辅助方法
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "convertValue", "(Ljava/lang/Object;)Ljava/lang/Object;", false);

            // 转换为目标类型
            if (paramType.isPrimitive()) {
                unboxPrimitive(mv, paramType);
            } else {
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
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
        } else {
            // 直接类型转换
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            if (paramType.isPrimitive()) {
                unboxPrimitive(mv, paramType);
            } else {
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
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
        }
        mv.visitMaxs(5, 4);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

    /**
     * 创建用于访问指定类型属性的写入器，通过生成如下代码:
     * <pre>{@code
     *     PropertyWriter{
     *
     *         void accept(Object o, Object o2){
     *             ((MyEntity)o).setName( (String) o2 );
     *         }
     *
     *     }
     *
     *  }</pre>
     *
     * @param clazz         类型
     * @param name          属性名称
     * @param typeConverter 类型转换器
     * @return PropertyWriter
     */
    public PropertyWriter createWriter(Class<?> clazz, String name, TypeConverter typeConverter) {
        try {
            PropertyDescriptor descriptor = findPropertyDescriptor(clazz, name);
            byte[] bytecode = createWriterCode(clazz,
                    generateClassName(clazz.getSimpleName() + "$" + descriptor.getName() + "$Writer"),
                    descriptor,
                    typeConverter);
            Class<?> generatedClass = defineClass(bytecode);
            
            // 预先计算ResolvableType，避免在构造方法中重复计算
            if (typeConverter != null) {
                ResolvableType resolvableType = ResolvableType.forMethodParameter(descriptor.getWriteMethod(), 0);
                return (PropertyWriter) generatedClass.getDeclaredConstructor(TypeConverter.class, ResolvableType.class)
                        .newInstance(typeConverter, resolvableType);
            } else {
                return (PropertyWriter) generatedClass.getDeclaredConstructor(TypeConverter.class)
                        .newInstance(typeConverter);
            }

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

    @SneakyThrows
    private Class<?> defineClass(byte[] bytes) {
        // 使用 MethodHandles.Lookup 来定义类，这是 Java 17 推荐的方式
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.defineClass(bytes);
    }

    @SneakyThrows
    private Class<?> defineClass(String className, byte[] bytes) {
        // 对于复杂的字节码，使用类加载器来定义类
        ClassLoader classLoader = this.getClass().getClassLoader();
        java.lang.reflect.Method defineClassMethod = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class);
        defineClassMethod.setAccessible(true);
        return (Class<?>) defineClassMethod.invoke(classLoader, className.replace('/', '.'), bytes, 0, bytes.length);
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

    /**
     * 生成优化的类型转换方法
     */
    private void generateOptimizedConvertMethod(ClassWriter cw, String className, Class<?> paramType, String resolvableTypeField) {
        MethodVisitor convertMv = cw.visitMethod(Opcodes.ACC_PRIVATE, "convertValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        convertMv.visitCode();

        // 检查值是否为null
        convertMv.visitVarInsn(Opcodes.ALOAD, 1);
        Label nullLabel = new Label();
        convertMv.visitJumpInsn(Opcodes.IFNULL, nullLabel);

        // 类型检查
        convertMv.visitVarInsn(Opcodes.ALOAD, 1);

        if (paramType.isPrimitive()) {
            // 基本类型需要检查对应的包装类型
            Class<?> wrapperType = getWrapperType(paramType);
            convertMv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(wrapperType));
            Label convertLabel = new Label();
            convertMv.visitJumpInsn(Opcodes.IFEQ, convertLabel);

            // 类型一致，直接返回
            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            convertMv.visitInsn(Opcodes.ARETURN);

            // 类型不一致，需要转换
            convertMv.visitLabel(convertLabel);
        } else {
            // 对象类型直接检查
            convertMv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(paramType));
            Label convertLabel = new Label();
            convertMv.visitJumpInsn(Opcodes.IFEQ, convertLabel);

            // 类型一致，直接返回
            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            convertMv.visitInsn(Opcodes.ARETURN);

            // 类型不一致，需要转换
            convertMv.visitLabel(convertLabel);
        }

        // 调用TypeConverter进行转换
        convertMv.visitVarInsn(Opcodes.ALOAD, 0);
        convertMv.visitFieldInsn(Opcodes.GETFIELD, className, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";");
        convertMv.visitVarInsn(Opcodes.ALOAD, 1);

        // 使用预先计算的ResolvableType字段
        convertMv.visitVarInsn(Opcodes.ALOAD, 0);
        convertMv.visitFieldInsn(Opcodes.GETFIELD, className, resolvableTypeField, "L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";");

        convertMv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_CONVERTER_INTERNAL_NAME, "convert", "(Ljava/lang/Object;L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";)Ljava/lang/Object;", true);
        convertMv.visitInsn(Opcodes.ARETURN);

        // 处理null值的情况
        convertMv.visitLabel(nullLabel);
        convertMv.visitVarInsn(Opcodes.ALOAD, 0);
        convertMv.visitFieldInsn(Opcodes.GETFIELD, className, "typeConverter", "L" + TYPE_CONVERTER_INTERNAL_NAME + ";");
        convertMv.visitInsn(Opcodes.ACONST_NULL);
        convertMv.visitVarInsn(Opcodes.ALOAD, 0);
        convertMv.visitFieldInsn(Opcodes.GETFIELD, className, resolvableTypeField, "L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";");
        convertMv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_CONVERTER_INTERNAL_NAME, "convert", "(Ljava/lang/Object;L" + RESOLVABLE_TYPE_INTERNAL_NAME + ";)Ljava/lang/Object;", true);
        convertMv.visitInsn(Opcodes.ARETURN);

        convertMv.visitMaxs(4, 2);
        convertMv.visitEnd();
    }
}
