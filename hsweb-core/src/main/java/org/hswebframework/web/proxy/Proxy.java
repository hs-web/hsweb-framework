package org.hswebframework.web.proxy;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 3.0
 */
public class Proxy<I> extends URLClassLoader {
    private static final AtomicLong counter = new AtomicLong(1);

    private final CtClass ctClass;
    @Getter
    private final Class<I> superClass;
    @Getter
    private final String className;
    @Getter
    private final String classFullName;

    private final Set<ClassLoader> loaders = new HashSet<>();
    private Class<I> targetClass;

    @SneakyThrows
    public static <I> Proxy<I> create(Class<I> superClass, Class<?>[] classPaths, String... classPathString) {
        return new Proxy<>(superClass, classPaths, classPathString);
    }

    @SneakyThrows
    public static <I> Proxy<I> create(Class<I> superClass, String... classPathString) {
        return new Proxy<>(superClass, null, classPathString);
    }

    public Proxy(Class<I> superClass, String... classPathString) {
        this(superClass, null, classPathString);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return super.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader loader : loaders) {
            URL resource = loader.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        @SuppressWarnings("all")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[loaders.size()];

        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                for (Enumeration<URL> urlEnumeration : tmp) {
                    if (urlEnumeration.hasMoreElements()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public URL nextElement() {
                for (Enumeration<URL> urlEnumeration : tmp) {
                    if (urlEnumeration.hasMoreElements()) {
                        return urlEnumeration.nextElement();
                    }
                }
                return null;
            }
        };
    }

    @SneakyThrows
    private static URL[] toUrl(String[] str) {
        if (str == null || str.length == 0) {
            return new URL[0];
        }
        URL[] arr = new URL[str.length];
        for (int i = 0; i < str.length; i++) {
            arr[i] = URI.create(str[i]).toURL();
        }
        return arr;
    }

    @SneakyThrows
    public Proxy(Class<I> superClass, Class<?>[] classPaths, String... classPathString) {
        super(toUrl(classPathString));
        if (superClass == null) {
            throw new NullPointerException("superClass can not be null");
        }
        this.superClass = superClass;
        ClassPool classPool = ClassPool.getDefault();

        if (classPaths != null) {
            for (Class<?> classPath : classPaths) {
                if (classPath.getClassLoader() != null &&
                    classPath.getClassLoader() != this.getClass().getClassLoader()) {
                    loaders.add(classPath.getClassLoader());
                }
            }
        }

        loaders.add(ClassUtils.getDefaultClassLoader());
        loaders.add(Proxy.class.getClassLoader());

        classPool.insertClassPath(new LoaderClassPath(this));

        className = superClass.getSimpleName() + "$Proxy" + counter.getAndIncrement();
        String packageName = superClass.getPackage().getName();
        if (packageName.startsWith("java")) {
            packageName = "proxy." + packageName;
        }
        classFullName = packageName + "." + className;

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
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(
                new ClassMemberValue(arr[0].getClass().getName(), constPool), constPool);
            arrayMemberValue.setValue(
                Arrays
                    .stream(arr)
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
        return getTargetClass().getConstructor().newInstance();
    }

    @SneakyThrows
    @SuppressWarnings("all")
    public Class<I> getTargetClass() {
        if (targetClass == null) {
            byte[] code = ctClass.toBytecode();
            targetClass = (Class) defineClass(null, code, 0, code.length);
        }
        return targetClass;
    }

    interface Task {
        void run() throws Exception;
    }
}
