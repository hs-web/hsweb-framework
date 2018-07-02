package org.hswebframework.web.proxy;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import javassist.scopedpool.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 3.0
 */
public class Proxy<I> {
    private static final AtomicLong counter = new AtomicLong(1);

    private CtClass  ctClass;
    @Getter
    private Class<I> superClass;
    @Getter
    private String   className;
    @Getter
    private String   classFullName;

    private Class<I> targetClass;

    @SneakyThrows
    public static <I> Proxy<I> create(Class<I> superClass, String... classPathString) {
        return new Proxy<>(superClass, classPathString);
    }

    @SneakyThrows
    public Proxy(Class<I> superClass, String... classPathString) {
        if (superClass == null) {
            throw new NullPointerException("superClass can not be null");
        }
        this.superClass = superClass;
        ClassPool classPool = new ClassPool(true);

        ClassPath classPath = new ClassClassPath(this.getClass());
        classPool.insertClassPath(classPath);
        if (classPathString != null) {
            for (String path : classPathString) {
                classPool.insertClassPath(path);
            }
        }
        className = superClass.getSimpleName() + "FastBeanCopier" + counter.getAndAdd(1);
        classFullName = superClass.getPackage() + "." + className;

        ctClass = classPool.makeClass(classFullName);
        if (superClass != Object.class) {
            if (superClass.isInterface()) {
                ctClass.setInterfaces(new CtClass[]{classPool.get(superClass.getName())});
            } else {
                ctClass.setSuperclass(classPool.get(superClass.getName()));
            }
        }
        addConstructor("public " + className + "(){}");
    }

    public Proxy<I> addMethod(String code) {
        return handleException(() -> ctClass.addMethod(CtNewMethod.make(code, ctClass)));
    }

    public Proxy<I> addConstructor(String code) {
        return handleException(() -> ctClass.addConstructor(CtNewConstructor.make(code, ctClass)));
    }

    public Proxy<I> addField(String code) {
        return addField(code, null);
    }

    public Proxy<I> addField(String code, Class<? extends java.lang.annotation.Annotation> annotation) {
        return addField(code, annotation, null);
    }

    @SuppressWarnings("all")
    public static MemberValue createMemberValue(Object value, ConstPool constPool) {
        MemberValue memberValue = null;
        if (value instanceof Integer) {
            memberValue = new IntegerMemberValue(constPool, ((Integer) value));
        } else if (value instanceof Boolean) {
            memberValue = new BooleanMemberValue((Boolean) value, constPool);
        } else if (value instanceof Long) {
            memberValue = new LongMemberValue((Long) value, constPool);
        } else if (value instanceof String) {
            memberValue = new StringMemberValue((String) value, constPool);
        } else if (value instanceof Class) {
            memberValue = new ClassMemberValue(((Class) value).getName(), constPool);
        } else if (value instanceof Object[]) {
            Object[] arr = ((Object[]) value);
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(new ClassMemberValue(arr[0].getClass().getName(), constPool), constPool);
            arrayMemberValue.setValue(Arrays.stream(arr)
                    .map(o -> createMemberValue(o, constPool))
                    .toArray(MemberValue[]::new));
            memberValue = arrayMemberValue;

        }
        return memberValue;
    }

    public Proxy<I> custom(Consumer<CtClass> ctClassConsumer) {
        ctClassConsumer.accept(ctClass);
        return this;
    }

    @SneakyThrows
    public Proxy<I> addField(String code, Class<? extends java.lang.annotation.Annotation> annotation, Map<String, Object> annotationProperties) {
        return handleException(() -> {
            CtField ctField = CtField.make(code, ctClass);
            if (null != annotation) {
                ConstPool constPool = ctClass.getClassFile().getConstPool();
                AnnotationsAttribute attributeInfo = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                Annotation ann = new javassist.bytecode.annotation.Annotation(annotation.getName(), constPool);
                if (null != annotationProperties) {
                    annotationProperties.forEach((key, value) -> {
                        MemberValue memberValue = createMemberValue(value, constPool);
                        if (memberValue != null) {
                            ann.addMemberValue(key, memberValue);
                        }
                    });
                }
                attributeInfo.addAnnotation(ann);
                ctField.getFieldInfo().addAttribute(attributeInfo);
            }
            ctClass.addField(ctField);
        });
    }

    @SneakyThrows
    private Proxy<I> handleException(Task task) {
        task.run();
        return this;
    }


    @SneakyThrows
    public I newInstance() {
        return getTargetClass().newInstance();
    }

    @SneakyThrows
    public Class<I> getTargetClass() {
        if (targetClass == null) {
            targetClass = ctClass.toClass(ClassUtils.getDefaultClassLoader(), null);
        }
        return targetClass;
    }

    interface Task {
        void run() throws Exception;
    }
}
