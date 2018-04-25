package org.hswebframework.web.proxy;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 3.0
 */
public class Proxy<I> {
    public              ClassPool  classPool = new ClassPool(true);
    public static final AtomicLong counter   = new AtomicLong(1);

    private CtClass  ctClass;
    @Getter
    private Class<I> superClass;
    @Getter
    private String   className;
    @Getter
    private String   classFullName;

    private Class<I> targetClass;

    public static <I> Proxy<I> create(Class<I> superClass, String... classPathString) {
        try {
            return new Proxy<>(superClass, classPathString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Proxy(Class<I> superClass, String... classPathString) throws Exception {
        if (superClass == null) {
            throw new NullPointerException("superClass can not be null");
        }
        this.superClass = superClass;
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
//        ctClass.debugWriteFile();
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

    public Proxy<I> addField(String code, String annotation) {
        return handleException(() -> {
            CtField ctField = CtField.make(code, ctClass);
            if (null != annotation) {
                ConstPool constPool = ctClass.getClassFile().getConstPool();
                AnnotationsAttribute attributeInfo =
                        new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                attributeInfo.addAnnotation(
                        new javassist.bytecode.annotation.Annotation(annotation, constPool));
                ctField.getFieldInfo().addAttribute(attributeInfo);
            }
            ctClass.addField(ctField);
        });
    }

    private Proxy<I> handleException(Task task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return this;
    }


    public I newInstance() {
        try {
            return getTargetClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<I> getTargetClass() {
        if (targetClass == null) {
            try {
                targetClass = ctClass.toClass();
            } catch (CannotCompileException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return targetClass;
    }

    interface Task {
        void run() throws Exception;
    }
}
