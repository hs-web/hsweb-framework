package org.hswebframework.web.bean.accessor;

import lombok.SneakyThrows;
import org.hswebframework.web.bean.Copier;
import org.hswebframework.web.bean.FastBeanCopierSupport;
import org.objectweb.asm.*;
import org.springframework.core.ResolvableType;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AsmBeanAccessor implements PropertyAccessor {

    private static final String PROPERTY_READER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyReader";
    private static final String CONVERTER_PROPERTY_TRANSFER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/ConverterPropertyTransfer";
    private static final String PROPERTY_TRANSFER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyTransfer";
    private static final String PROPERTY_WRITER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/PropertyWriter";
    private static final String COPIER_INTERNAL_NAME = "org/hswebframework/web/bean/Copier";
    private static final String BEAN_CONVERTER_INTERNAL_NAME = "org/hswebframework/web/bean/Converter";
    private static final String TYPE_CONVERTER_INTERNAL_NAME = "org/hswebframework/web/bean/accessor/TypeConverter";
    private static final String RESOLVABLE_TYPE_INTERNAL_NAME = "org/springframework/core/ResolvableType";
    private static final ClassLoader ACCESSOR_CLASS_LOADER = AsmBeanAccessor.class.getClassLoader();

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final PropertyAccessor fallback = new ReflectionBeanAccessor();

    public Copier createDirectCopier(Class<?> sourceClass, Class<?> targetClass) {
        if (shouldUseFallback(sourceClass, targetClass)) {
            return null;
        }
        try {
            Map<String, PropertyDescriptor> sourceProperties = findWritableReadableProperties(sourceClass, true);
            Map<String, PropertyDescriptor> targetProperties = findWritableReadableProperties(targetClass, false);
            if (sourceProperties.isEmpty() || targetProperties.isEmpty()) {
                return null;
            }
            Map<PropertyDescriptor, PropertyDescriptor> mappings = new LinkedHashMap<>();
            for (PropertyDescriptor sourceProperty : sourceProperties.values()) {
                PropertyDescriptor targetProperty = targetProperties.get(sourceProperty.getName());
                if (targetProperty == null) {
                    continue;
                }
                if (!canUseDirectCopier(sourceProperty, targetProperty)) {
                    return null;
                }
                mappings.put(sourceProperty, targetProperty);
            }
            if (mappings.isEmpty()) {
                return null;
            }
            byte[] bytecode = createDirectCopierCode(sourceClass, targetClass, mappings);
            Class<?> generatedClass = defineClass(bytecode);
            return (Copier) generatedClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            return null;
        }
    }

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
        if (shouldUseFallback(clazz)) {
            return fallback.createReader(clazz, name);
        }
        try {
            Class<?> generatedClass = defineClass(createReaderCode(clazz, name));
            return (PropertyReader) generatedClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return fallback.createReader(clazz, name);
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
        if (shouldUseFallback(clazz)) {
            return fallback.createWriter(clazz, name, typeConverter);
        }
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
            return fallback.createWriter(clazz, name, typeConverter);
        }
    }

    @Override
    public PropertyTransfer createTransfer(Class<?> sourceClass,
                                           String sourceName,
                                           boolean sourcePrimitive,
                                           Class<?> targetClass,
                                           String targetName) {
        if (shouldUseFallback(sourceClass, targetClass)) {
            return fallback.createTransfer(sourceClass, sourceName, sourcePrimitive, targetClass, targetName);
        }
        try {
            PropertyDescriptor sourceDescriptor = findPropertyDescriptor(sourceClass, sourceName);
            PropertyDescriptor targetDescriptor = findPropertyDescriptor(targetClass, targetName);
            if (sourceDescriptor == null
                || sourceDescriptor.getReadMethod() == null
                || targetDescriptor == null
                || targetDescriptor.getWriteMethod() == null) {
                return null;
            }
            byte[] bytecode = createTransferCode(sourceClass,
                                                 sourceDescriptor,
                                                 sourcePrimitive,
                                                 targetClass,
                                                 targetDescriptor);
            Class<?> generatedClass = defineClass(bytecode);
            return (PropertyTransfer) generatedClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            return fallback.createTransfer(sourceClass, sourceName, sourcePrimitive, targetClass, targetName);
        }
    }

    @Override
    public ConverterPropertyTransfer createConverterTransfer(Class<?> sourceClass,
                                                             String sourceName,
                                                             boolean sourcePrimitive,
                                                             Class<?> targetClass,
                                                             String targetName,
                                                             boolean allowDirectAssignment) {
        if (shouldUseFallback(sourceClass, targetClass)) {
            return fallback.createConverterTransfer(sourceClass,
                                                    sourceName,
                                                    sourcePrimitive,
                                                    targetClass,
                                                    targetName,
                                                    allowDirectAssignment);
        }
        try {
            PropertyDescriptor sourceDescriptor = findPropertyDescriptor(sourceClass, sourceName);
            PropertyDescriptor targetDescriptor = findPropertyDescriptor(targetClass, targetName);
            if (sourceDescriptor == null
                || sourceDescriptor.getReadMethod() == null
                || targetDescriptor == null
                || targetDescriptor.getWriteMethod() == null) {
                return null;
            }
            byte[] bytecode = createConverterTransferCode(sourceClass,
                                                          sourceDescriptor,
                                                          sourcePrimitive,
                                                          targetClass,
                                                          targetDescriptor,
                                                          allowDirectAssignment);
            Class<?> generatedClass = defineClass(bytecode);
            Class<?>[] genericTypes = resolveGenericTypes(
                ResolvableType.forMethodParameter(targetDescriptor.getWriteMethod(), 0, targetClass)
            );
            return (ConverterPropertyTransfer) generatedClass.getDeclaredConstructor(Class[].class)
                .newInstance((Object) genericTypes);
        } catch (Throwable e) {
            return fallback.createConverterTransfer(sourceClass,
                                                    sourceName,
                                                    sourcePrimitive,
                                                    targetClass,
                                                    targetName,
                                                    allowDirectAssignment);
        }
    }

    public byte[] createTransferCode(Class<?> sourceClass,
                                     PropertyDescriptor sourceDescriptor,
                                     boolean sourcePrimitive,
                                     Class<?> targetClass,
                                     PropertyDescriptor targetDescriptor) {
        if (sourceDescriptor == null || sourceDescriptor.getReadMethod() == null) {
            throw new IllegalArgumentException("Property '" + sourceDescriptor + "' not found or not readable in class " + sourceClass.getName());
        }
        if (targetDescriptor == null || targetDescriptor.getWriteMethod() == null) {
            throw new IllegalArgumentException("Property '" + targetDescriptor + "' not found or not writable in class " + targetClass.getName());
        }

        Method readMethod = sourceDescriptor.getReadMethod();
        Method writeMethod = targetDescriptor.getWriteMethod();
        Class<?> readType = readMethod.getReturnType();
        Class<?> targetParamType = writeMethod.getParameterTypes()[0];
        String className = generateClassName(sourceClass.getSimpleName()
                                             + "$" + sourceDescriptor.getName()
                                             + "$To$"
                                             + targetClass.getSimpleName()
                                             + "$" + targetDescriptor.getName()
                                             + "$Transfer");

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{PROPERTY_TRANSFER_INTERNAL_NAME});

        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "transfer", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(sourceClass));
        mv.visitMethodInsn(readMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(readMethod.getDeclaringClass()),
                           readMethod.getName(),
                           Type.getMethodDescriptor(readMethod),
                           readMethod.getDeclaringClass().isInterface());

        Label returnLabel = new Label();
        if (readType.isPrimitive()) {
            mv.visitVarInsn(getStoreOpcode(readType), 3);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetClass));
            mv.visitVarInsn(getLoadOpcode(readType), 3);
            adaptPrimitiveValueForTarget(mv, readType, targetParamType);
        } else {
            mv.visitVarInsn(Opcodes.ASTORE, 3);
            if (!sourcePrimitive) {
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitJumpInsn(Opcodes.IFNULL, returnLabel);
            }
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetClass));
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            adaptReferenceValueForTarget(mv, targetParamType);
        }

        mv.visitMethodInsn(writeMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(writeMethod.getDeclaringClass()),
                           writeMethod.getName(),
                           Type.getMethodDescriptor(writeMethod),
                           writeMethod.getDeclaringClass().isInterface());
        mv.visitLabel(returnLabel);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    public byte[] createDirectCopierCode(Class<?> sourceClass,
                                         Class<?> targetClass,
                                         Map<PropertyDescriptor, PropertyDescriptor> mappings) {
        String className = generateClassName(sourceClass.getSimpleName()
                                             + "$To$"
                                             + targetClass.getSimpleName()
                                             + "$DirectCopier");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{COPIER_INTERNAL_NAME});

        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "copy",
                                          "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/Set;L" + BEAN_CONVERTER_INTERNAL_NAME + ";)V",
                                          null,
                                          null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(sourceClass));
        mv.visitVarInsn(Opcodes.ASTORE, 5);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetClass));
        mv.visitVarInsn(Opcodes.ASTORE, 6);

        Label withIgnore = new Label();
        Label end = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "isEmpty", "()Z", true);
        mv.visitJumpInsn(Opcodes.IFEQ, withIgnore);
        for (Map.Entry<PropertyDescriptor, PropertyDescriptor> entry : mappings.entrySet()) {
            generateDirectCopyStatement(mv, sourceClass, targetClass, entry.getKey(), entry.getValue(), false);
        }
        mv.visitJumpInsn(Opcodes.GOTO, end);

        mv.visitLabel(withIgnore);
        for (Map.Entry<PropertyDescriptor, PropertyDescriptor> entry : mappings.entrySet()) {
            String name = entry.getKey().getName();
            Label next = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
            mv.visitJumpInsn(Opcodes.IFNE, next);
            generateDirectCopyStatement(mv, sourceClass, targetClass, entry.getKey(), entry.getValue(), true);
            mv.visitLabel(next);
        }
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    public byte[] createConverterTransferCode(Class<?> sourceClass,
                                              PropertyDescriptor sourceDescriptor,
                                              boolean sourcePrimitive,
                                              Class<?> targetClass,
                                              PropertyDescriptor targetDescriptor,
                                              boolean allowDirectAssignment) {
        if (sourceDescriptor == null || sourceDescriptor.getReadMethod() == null) {
            throw new IllegalArgumentException("Property '" + sourceDescriptor + "' not found or not readable in class " + sourceClass.getName());
        }
        if (targetDescriptor == null || targetDescriptor.getWriteMethod() == null) {
            throw new IllegalArgumentException("Property '" + targetDescriptor + "' not found or not writable in class " + targetClass.getName());
        }

        Method readMethod = sourceDescriptor.getReadMethod();
        Method writeMethod = targetDescriptor.getWriteMethod();
        Class<?> readType = readMethod.getReturnType();
        Class<?> targetParamType = writeMethod.getParameterTypes()[0];
        String className = generateClassName(sourceClass.getSimpleName()
                                             + "$" + sourceDescriptor.getName()
                                             + "$To$"
                                             + targetClass.getSimpleName()
                                             + "$" + targetDescriptor.getName()
                                             + "$ConvertingTransfer");

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", new String[]{CONVERTER_PROPERTY_TRANSFER_INTERNAL_NAME});
        FieldVisitor genericTypesField = cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                                       "genericTypes",
                                                       "[Ljava/lang/Class;",
                                                       null,
                                                       null);
        genericTypesField.visitEnd();

        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                                   "<init>",
                                                   "([Ljava/lang/Class;)V",
                                                   null,
                                                   null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitVarInsn(Opcodes.ALOAD, 1);
        constructor.visitFieldInsn(Opcodes.PUTFIELD, className, "genericTypes", "[Ljava/lang/Class;");
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();

        generateTransferConvertMethod(cw,
                                      className,
                                      targetParamType,
                                      allowDirectAssignment,
                                      isNumberType(targetParamType));

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "transfer",
                                          "(Ljava/lang/Object;Ljava/lang/Object;L" + BEAN_CONVERTER_INTERNAL_NAME + ";)V",
                                          null,
                                          null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(sourceClass));
        mv.visitMethodInsn(readMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(readMethod.getDeclaringClass()),
                           readMethod.getName(),
                           Type.getMethodDescriptor(readMethod),
                           readMethod.getDeclaringClass().isInterface());

        if (readType.isPrimitive()) {
            boxPrimitive(mv, readType);
        }
        mv.visitVarInsn(Opcodes.ASTORE, 4);

        Label returnLabel = new Label();
        if (!sourcePrimitive) {
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitJumpInsn(Opcodes.IFNULL, returnLabel);
        }

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                           className,
                           "convertValue",
                           "(Ljava/lang/Object;L" + BEAN_CONVERTER_INTERNAL_NAME + ";)Ljava/lang/Object;",
                           false);
        mv.visitVarInsn(Opcodes.ASTORE, 4);

        if (!targetParamType.isPrimitive()) {
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitJumpInsn(Opcodes.IFNULL, returnLabel);
        }

        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetClass));
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        adaptReferenceValueForTarget(mv, targetParamType);
        mv.visitMethodInsn(writeMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(writeMethod.getDeclaringClass()),
                           writeMethod.getName(),
                           Type.getMethodDescriptor(writeMethod),
                           writeMethod.getDeclaringClass().isInterface());
        mv.visitLabel(returnLabel);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
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

    private Map<String, PropertyDescriptor> findWritableReadableProperties(Class<?> clazz, boolean source) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        Map<String, PropertyDescriptor> properties = new LinkedHashMap<>();
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            if ("class".equals(property.getName())) {
                continue;
            }
            if (source) {
                if (property.getReadMethod() == null) {
                    continue;
                }
            } else if (property.getWriteMethod() == null) {
                continue;
            }
            properties.put(property.getName(), property);
        }
        return properties;
    }

    private String generateClassName(String prefix) {
        return "org/hswebframework/web/bean/accessor/" + prefix + "$" + COUNTER.incrementAndGet();
    }

    private boolean canUseDirectCopier(PropertyDescriptor sourceProperty, PropertyDescriptor targetProperty) {
        Method readMethod = sourceProperty.getReadMethod();
        Method writeMethod = targetProperty.getWriteMethod();
        if (readMethod == null || writeMethod == null) {
            return false;
        }
        if (!Modifier.isPublic(readMethod.getModifiers()) || !Modifier.isPublic(writeMethod.getModifiers())) {
            return false;
        }
        Class<?> sourceType = readMethod.getReturnType();
        Class<?> targetType = writeMethod.getParameterTypes()[0];
        return isDirectCompatible(sourceType, targetType);
    }

    private boolean isDirectCompatible(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) {
            return true;
        }
        if (sourceType.isPrimitive()) {
            return getWrapperType(sourceType) == targetType || targetType == Object.class;
        }
        if (targetType.isPrimitive()) {
            return getWrapperType(targetType) == sourceType;
        }
        return targetType.isAssignableFrom(sourceType);
    }

    private boolean shouldClone(Class<?> sourceType, Class<?> targetType) {
        return sourceType == targetType
            && sourceType.isArray();
    }

    private void generateDirectCopyStatement(MethodVisitor mv,
                                             Class<?> sourceClass,
                                             Class<?> targetClass,
                                             PropertyDescriptor sourceDescriptor,
                                             PropertyDescriptor targetDescriptor,
                                             boolean ignoreBranch) {
        Method readMethod = sourceDescriptor.getReadMethod();
        Method writeMethod = targetDescriptor.getWriteMethod();
        Class<?> sourceType = readMethod.getReturnType();
        Class<?> targetType = writeMethod.getParameterTypes()[0];
        Label skip = new Label();

        if (sourceType.isPrimitive()) {
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitMethodInsn(readMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                               Type.getInternalName(readMethod.getDeclaringClass()),
                               readMethod.getName(),
                               Type.getMethodDescriptor(readMethod),
                               readMethod.getDeclaringClass().isInterface());
            adaptPrimitiveValueForTarget(mv, sourceType, targetType);
            mv.visitMethodInsn(writeMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                               Type.getInternalName(writeMethod.getDeclaringClass()),
                               writeMethod.getName(),
                               Type.getMethodDescriptor(writeMethod),
                               writeMethod.getDeclaringClass().isInterface());
            return;
        }

        mv.visitVarInsn(Opcodes.ALOAD, 5);
        mv.visitMethodInsn(readMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(readMethod.getDeclaringClass()),
                           readMethod.getName(),
                           Type.getMethodDescriptor(readMethod),
                           readMethod.getDeclaringClass().isInterface());
        mv.visitVarInsn(Opcodes.ASTORE, 7);
        Label continueLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 7);
        mv.visitJumpInsn(Opcodes.IFNONNULL, continueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, skip);
        mv.visitLabel(continueLabel);

        mv.visitVarInsn(Opcodes.ALOAD, 6);
        if (shouldClone(sourceType, targetType)) {
            mv.visitVarInsn(Opcodes.ALOAD, 7);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                               Type.getInternalName(AsmBeanAccessor.class),
                               "cloneValue",
                               "(Ljava/lang/Object;)Ljava/lang/Object;",
                               false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 7);
        }
        adaptReferenceValueForTarget(mv, targetType);
        mv.visitMethodInsn(writeMethod.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                           Type.getInternalName(writeMethod.getDeclaringClass()),
                           writeMethod.getName(),
                           Type.getMethodDescriptor(writeMethod),
                           writeMethod.getDeclaringClass().isInterface());
        mv.visitLabel(skip);
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

    private void adaptPrimitiveValueForTarget(MethodVisitor mv, Class<?> sourceType, Class<?> targetType) {
        if (targetType.isPrimitive()) {
            return;
        }
        boxPrimitive(mv, sourceType);
        if (targetType != Object.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetType));
        }
    }

    private void adaptReferenceValueForTarget(MethodVisitor mv, Class<?> targetType) {
        if (targetType.isPrimitive()) {
            unboxPrimitive(mv, targetType);
            return;
        }
        if (targetType != Object.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetType));
        }
    }

    private int getStoreOpcode(Class<?> type) {
        if (type == long.class) {
            return Opcodes.LSTORE;
        }
        if (type == float.class) {
            return Opcodes.FSTORE;
        }
        if (type == double.class) {
            return Opcodes.DSTORE;
        }
        return Opcodes.ISTORE;
    }

    private int getLoadOpcode(Class<?> type) {
        if (type == long.class) {
            return Opcodes.LLOAD;
        }
        if (type == float.class) {
            return Opcodes.FLOAD;
        }
        if (type == double.class) {
            return Opcodes.DLOAD;
        }
        return Opcodes.ILOAD;
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

    public static Object cloneValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(value);
                Object clone = java.lang.reflect.Array.newInstance(value.getClass().getComponentType(), length);
                System.arraycopy(value, 0, clone, 0, length);
                return clone;
            }
            Method clone = value.getClass().getMethod("clone");
            clone.setAccessible(true);
            return clone.invoke(value);
        } catch (Throwable ignore) {
            return value;
        }
    }

    private void generateTransferConvertMethod(ClassWriter cw,
                                               String className,
                                               Class<?> paramType,
                                               boolean allowDirectAssignment,
                                               boolean enumDictToNumber) {
        MethodVisitor convertMv = cw.visitMethod(Opcodes.ACC_PRIVATE,
                                                 "convertValue",
                                                 "(Ljava/lang/Object;L" + BEAN_CONVERTER_INTERNAL_NAME + ";)Ljava/lang/Object;",
                                                 null,
                                                 null);
        convertMv.visitCode();

        Label convertLabel = new Label();

        if (enumDictToNumber) {
            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            convertMv.visitTypeInsn(Opcodes.INSTANCEOF, "org/hswebframework/web/dict/EnumDict");
            Label skipEnumDictUnwrapLabel = new Label();
            convertMv.visitJumpInsn(Opcodes.IFEQ, skipEnumDictUnwrapLabel);

            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            convertMv.visitTypeInsn(Opcodes.CHECKCAST, "org/hswebframework/web/dict/EnumDict");
            convertMv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                                      "org/hswebframework/web/dict/EnumDict",
                                      "getValue",
                                      "()Ljava/lang/Object;",
                                      true);
            convertMv.visitVarInsn(Opcodes.ASTORE, 3);
            convertMv.visitVarInsn(Opcodes.ALOAD, 3);
            Label skipNullEnumValueLabel = new Label();
            convertMv.visitJumpInsn(Opcodes.IFNULL, skipNullEnumValueLabel);
            convertMv.visitVarInsn(Opcodes.ALOAD, 3);
            convertMv.visitVarInsn(Opcodes.ASTORE, 1);
            convertMv.visitLabel(skipNullEnumValueLabel);
            convertMv.visitLabel(skipEnumDictUnwrapLabel);
        }

        if (allowDirectAssignment) {
            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            if (paramType.isPrimitive()) {
                Class<?> wrapperType = getWrapperType(paramType);
                convertMv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(wrapperType));
            } else {
                convertMv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(paramType));
            }
            convertMv.visitJumpInsn(Opcodes.IFEQ, convertLabel);
            convertMv.visitVarInsn(Opcodes.ALOAD, 1);
            convertMv.visitInsn(Opcodes.ARETURN);
        }

        convertMv.visitLabel(convertLabel);
        convertMv.visitVarInsn(Opcodes.ALOAD, 2);
        convertMv.visitVarInsn(Opcodes.ALOAD, 1);
        convertMv.visitLdcInsn(Type.getType(paramType));
        convertMv.visitVarInsn(Opcodes.ALOAD, 0);
        convertMv.visitFieldInsn(Opcodes.GETFIELD, className, "genericTypes", "[Ljava/lang/Class;");
        convertMv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                                  BEAN_CONVERTER_INTERNAL_NAME,
                                  "convert",
                                  "(Ljava/lang/Object;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/Object;",
                                  true);
        convertMv.visitInsn(Opcodes.ARETURN);
        convertMv.visitMaxs(0, 0);
        convertMv.visitEnd();
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
        return arr.length == 0 ? FastBeanCopierSupport.EMPTY_CLASS_ARRAY : arr;
    }

    private boolean shouldUseFallback(Class<?>... types) {
        java.util.Map<String, Class<?>> names = new java.util.HashMap<>();
        for (Class<?> type : types) {
            Class<?> actualType = normalizeType(type);
            if (actualType.isPrimitive()) {
                continue;
            }
            Class<?> exists = names.putIfAbsent(actualType.getName(), actualType);
            if (exists != null && exists != actualType) {
                return true;
            }
            if (!isTypeVisible(actualType)) {
                return true;
            }
        }
        return false;
    }

    private Class<?> normalizeType(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }

    private boolean isTypeVisible(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        if (loader == null || loader == ACCESSOR_CLASS_LOADER) {
            return true;
        }
        try {
            return Class.forName(type.getName(), false, ACCESSOR_CLASS_LOADER) == type;
        } catch (Throwable ignore) {
            return false;
        }
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
