package org.hswebframework.web.bean;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.proxy.Proxy;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.springframework.core.ResolvableType;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Record constructor copier support.
 * <p>
 * Record is immutable, so copy-to-record cannot follow the normal in-place
 * {@link Copier} contract. This support generates a small constructor copier
 * and reuses the common {@link Converter} for nested type conversion.
 *
 * @author zhouhao
 * @since 5.0.2
 */
@Slf4j
final class RecordBeanCopierSupport {
    static final String ASM_DISABLED_PROPERTY = "hsweb.fastBeanCopier.record.asm.disabled";

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();
    private static final AtomicInteger ASM_COUNTER = new AtomicInteger();

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

    private RecordBeanCopierSupport() {
    }

    static RecordCopier createRecordCopier(Class<?> source, Class<?> target) {
        if (!Boolean.getBoolean(ASM_DISABLED_PROPERTY)) {
            RecordCopier asmCopier = tryCreateAsmRecordCopier(source, target);
            if (asmCopier != null) {
                return asmCopier;
            }
        }
        String method = "public Object copy(Object s, java.util.Set ignore, " +
            "org.hswebframework.web.bean.Converter converter){\n" +
            "try{\n\t" +
            createRecordCopierCode(source, target) +
            "}catch(Throwable e){\n" +
            "\tthrow new UnsupportedOperationException(e.getMessage(), e);" +
            "\n}\n" +
            "\n}";
        try {
            @SuppressWarnings("all")
            Proxy<RecordCopier> proxy = Proxy
                .create(RecordCopier.class, new Class[]{source, target})
                .addMethod(method);
            return proxy.newInstance();
        } catch (Exception e) {
            log.error("创建record copy代理对象失败:\n{}", method, e);
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    private static RecordCopier tryCreateAsmRecordCopier(Class<?> source, Class<?> target) {
        if (!canUseAsmRecordCopier(source, target)) {
            return null;
        }
        try {
            Class<?> generatedClass = MethodHandles.lookup().defineClass(createAsmRecordCopierCode(source, target));
            return (RecordCopier) generatedClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            log.debug("创建ASM record copy代理对象失败:{}=>{}", source, target, e);
            return null;
        }
    }

    private static boolean canUseAsmRecordCopier(Class<?> source, Class<?> target) {
        if (Map.class.isAssignableFrom(source)
            || !target.isRecord()
            || !isPublicVisible(source)
            || !isPublicVisible(target)) {
            return false;
        }
        Map<String, FastBeanCopierPropertySupport.ClassProperty> sourceProperties =
            FastBeanCopierPropertySupport.createProperty(source);
        for (RecordComponent component : target.getRecordComponents()) {
            FastBeanCopierPropertySupport.ClassProperty sourceProperty = sourceProperties.get(component.getName());
            if (sourceProperty == null) {
                continue;
            }
            if (!isDirectCompatible(sourceProperty.getType(), component.getType())) {
                return false;
            }
            Method accessor = findReadMethod(sourceProperty);
            if (accessor == null
                || !Modifier.isPublic(accessor.getModifiers())
                || !isPublicVisible(accessor.getDeclaringClass())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPublicVisible(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type.isPrimitive() || Modifier.isPublic(type.getModifiers());
    }

    private static boolean isDirectCompatible(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) {
            return true;
        }
        if (sourceType.isPrimitive()) {
            return PRIMITIVE_WRAPPERS.get(sourceType) == targetType || targetType == Object.class;
        }
        if (targetType.isPrimitive()) {
            return PRIMITIVE_WRAPPERS.get(targetType) == sourceType;
        }
        return targetType.isAssignableFrom(sourceType);
    }

    private static Method findReadMethod(FastBeanCopierPropertySupport.ClassProperty property) {
        try {
            return property.getBeanType().getMethod(property.getReadMethodName());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static byte[] createAsmRecordCopierCode(Class<?> source, Class<?> target) {
        String className = "org/hswebframework/web/bean/RecordCopier$Asm" + ASM_COUNTER.incrementAndGet();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8,
                 Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                 className,
                 null,
                 "java/lang/Object",
                 new String[]{"org/hswebframework/web/bean/RecordCopier"});

        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                                          "copy",
                                          "(Ljava/lang/Object;Ljava/util/Set;Lorg/hswebframework/web/bean/Converter;)Ljava/lang/Object;",
                                          null,
                                          null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(source));
        mv.visitVarInsn(Opcodes.ASTORE, 4);
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(target));
        mv.visitInsn(Opcodes.DUP);

        Map<String, FastBeanCopierPropertySupport.ClassProperty> sourceProperties =
            FastBeanCopierPropertySupport.createProperty(source);
        StringBuilder constructorDescriptor = new StringBuilder("(");
        for (RecordComponent component : target.getRecordComponents()) {
            Class<?> componentType = component.getType();
            constructorDescriptor.append(Type.getDescriptor(componentType));
            FastBeanCopierPropertySupport.ClassProperty sourceProperty = sourceProperties.get(component.getName());
            generateAsmRecordConstructorArgument(mv, component, sourceProperty);
        }
        constructorDescriptor.append(")V");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                           Type.getInternalName(target),
                           "<init>",
                           constructorDescriptor.toString(),
                           false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void generateAsmRecordConstructorArgument(MethodVisitor mv,
                                                            RecordComponent component,
                                                            FastBeanCopierPropertySupport.ClassProperty sourceProperty) {
        Class<?> targetType = component.getType();
        Label readValue = new Label();
        Label end = new Label();

        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitLdcInsn(component.getName());
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                           "java/util/Set",
                           "contains",
                           "(Ljava/lang/Object;)Z",
                           true);
        mv.visitJumpInsn(Opcodes.IFEQ, readValue);
        pushDefaultValue(mv, targetType);
        mv.visitJumpInsn(Opcodes.GOTO, end);

        mv.visitLabel(readValue);
        if (sourceProperty == null) {
            pushDefaultValue(mv, targetType);
            mv.visitJumpInsn(Opcodes.GOTO, end);
        } else {
            Method accessor = Objects.requireNonNull(findReadMethod(sourceProperty));
            Class<?> sourceType = accessor.getReturnType();
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitMethodInsn(accessor.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                               Type.getInternalName(accessor.getDeclaringClass()),
                               accessor.getName(),
                               Type.getMethodDescriptor(accessor),
                               accessor.getDeclaringClass().isInterface());
            adaptDirectValueForTarget(mv, sourceType, targetType);
        }
        mv.visitLabel(end);
    }

    private static void adaptDirectValueForTarget(MethodVisitor mv, Class<?> sourceType, Class<?> targetType) {
        if (sourceType.isPrimitive()) {
            if (targetType.isPrimitive()) {
                return;
            }
            boxPrimitive(mv, sourceType);
            if (targetType != Object.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetType));
            }
            return;
        }
        Label notNull = new Label();
        Label end = new Label();
        mv.visitInsn(Opcodes.DUP);
        mv.visitJumpInsn(Opcodes.IFNONNULL, notNull);
        mv.visitInsn(Opcodes.POP);
        pushDefaultValue(mv, targetType);
        mv.visitJumpInsn(Opcodes.GOTO, end);
        mv.visitLabel(notNull);
        if (targetType.isPrimitive()) {
            unboxPrimitive(mv, targetType);
        } else if (targetType != Object.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetType));
        }
        mv.visitLabel(end);
    }

    private static String createRecordCopierCode(Class<?> source, Class<?> target) {
        Map<String, FastBeanCopierPropertySupport.ClassProperty> sourceProperties =
            Map.class.isAssignableFrom(source)
                ? FastBeanCopierPropertySupport.createMapProperty(FastBeanCopierPropertySupport.createRecordProperty(target))
                : FastBeanCopierPropertySupport.createProperty(source);
        String sourceTypeName = getTypeName(source);
        String targetTypeName = getTypeName(target);
        boolean sourceIsMap = Map.class.isAssignableFrom(source);
        StringBuilder code = new StringBuilder();
        code.append(sourceTypeName).append(" $$__source=(").append(sourceTypeName).append(")s;\n\t");

        RecordComponent[] components = target.getRecordComponents();
        List<String> constructorArgs = new ArrayList<>(components.length);
        for (RecordComponent component : components) {
            String name = component.getName();
            Class<?> type = component.getType();
            String typeName = getTypeName(type);
            String varName = "$$__" + name;
            code.append(typeName).append(" ").append(varName).append("=").append(defaultValueCode(type)).append(";\n\t");
            code.append("if(!ignore.contains(\"").append(name).append("\")){\n\t\t");

            FastBeanCopierPropertySupport.ClassProperty sourceProperty = sourceProperties.get(name);
            if (sourceProperty == null) {
                code.append("// source property not found, keep default value.\n\t");
                code.append("}\n\t");
                constructorArgs.add(varName);
                continue;
            }

            String valueExpression = sourceIsMap
                ? "$$__source.get(\"" + name + "\")"
                : "$$__source." + sourceProperty.getReadMethod();
            if (sourceProperty.isPrimitive()) {
                valueExpression = PRIMITIVE_WRAPPERS
                    .get(sourceProperty.getType())
                    .getName() + ".valueOf(" + valueExpression + ")";
            }
            String generic = resolveRecordGenericCode(component);
            code.append("Object $$__value=(Object)(").append(valueExpression).append(");\n\t\t");
            code.append("if($$__value!=null){\n\t\t\t");
            if (requiresRecordGenericConversion(component, type)) {
                code.append(varName).append("=").append(convertValueCode(type, generic)).append(";\n\t\t");
            } else {
                code.append("if(").append(directAssignableCode(type, "$$__value")).append("){\n\t\t\t");
                code.append(varName).append("=").append(castValueCode(type, "$$__value")).append(";\n\t\t\t");
                code.append("}else{\n\t\t\t");
                code.append(varName).append("=").append(convertValueCode(type, generic)).append(";\n\t\t\t");
                code.append("}\n\t\t");
            }
            code.append("}\n\t");
            code.append("}\n\t");
            constructorArgs.add(varName);
        }
        code.append("return new ").append(targetTypeName).append("(")
            .append(String.join(",", constructorArgs))
            .append(");\n");
        return code.toString();
    }

    private static boolean requiresRecordGenericConversion(RecordComponent component, Class<?> type) {
        return ResolvableType.forType(component.getGenericType()).hasGenerics()
            && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));
    }

    private static String resolveRecordGenericCode(RecordComponent component) {
        Class<?>[] genericTypes = Arrays.stream(ResolvableType.forType(component.getGenericType()).getGenerics())
            .map(ResolvableType::resolve)
            .filter(Objects::nonNull)
            .toArray(Class[]::new);
        if (genericTypes.length == 0) {
            return "org.hswebframework.web.bean.FastBeanCopierSupport.EMPTY_CLASS_ARRAY";
        }
        return "new Class[]{" + Arrays.stream(genericTypes)
            .map(type -> getTypeName(type) + ".class")
            .collect(Collectors.joining(",")) + "}";
    }

    private static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            return getTypeName(type.getComponentType()) + "[]";
        }
        return type.getName();
    }

    private static String defaultValueCode(Class<?> type) {
        if (!type.isPrimitive()) {
            return "null";
        }
        if (type == boolean.class) {
            return "false";
        }
        if (type == char.class) {
            return "(char)0";
        }
        if (type == byte.class) {
            return "(byte)0";
        }
        if (type == short.class) {
            return "(short)0";
        }
        if (type == long.class) {
            return "0L";
        }
        if (type == float.class) {
            return "0F";
        }
        if (type == double.class) {
            return "0D";
        }
        return "0";
    }

    private static String castValueCode(Class<?> type, String value) {
        if (!type.isPrimitive()) {
            return "(" + getTypeName(type) + ")" + value;
        }
        Class<?> wrapper = PRIMITIVE_WRAPPERS.get(type);
        return "((" + wrapper.getName() + ")" + value + ")." + type.getName() + "Value()";
    }

    private static String convertValueCode(Class<?> type, String generic) {
        String converted = "converter.convert($$__value," + getTypeName(type) + ".class," + generic + ")";
        if (!type.isPrimitive()) {
            return "(" + getTypeName(type) + ")" + converted;
        }
        Class<?> wrapper = PRIMITIVE_WRAPPERS.get(type);
        return "((" + wrapper.getName() + ")" + converted + ")." + type.getName() + "Value()";
    }

    private static String directAssignableCode(Class<?> type, String value) {
        Class<?> directType = type.isPrimitive()
            ? PRIMITIVE_WRAPPERS.get(type)
            : type;
        return value + " instanceof " + getTypeName(directType);
    }

    private static void pushDefaultValue(MethodVisitor mv, Class<?> type) {
        if (!type.isPrimitive()) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        if (type == long.class) {
            mv.visitInsn(Opcodes.LCONST_0);
        } else if (type == float.class) {
            mv.visitInsn(Opcodes.FCONST_0);
        } else if (type == double.class) {
            mv.visitInsn(Opcodes.DCONST_0);
        } else {
            mv.visitInsn(Opcodes.ICONST_0);
        }
    }

    private static void boxPrimitive(MethodVisitor mv, Class<?> primitiveType) {
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

    private static void unboxPrimitive(MethodVisitor mv, Class<?> primitiveType) {
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
}
