package org.hswebframework.web.loggin.aop;


import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.hswebframework.web.logging.LoggerDefine;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ResourceAccessLoggerParser implements AccessLoggerParser {

    Set<Class<? extends Annotation>> annotations = new HashSet<>(Arrays.asList(
            Resource.class, ResourceAction.class
    ));

    @Override
    public boolean support(Class clazz, Method method) {
        Set<Annotation> a1 = AnnotatedElementUtils.findAllMergedAnnotations(method, annotations);
        Set<Annotation> a2 = AnnotatedElementUtils.findAllMergedAnnotations(clazz, annotations);


        return !a1.isEmpty() || !a2.isEmpty();
    }

    @Override
    public LoggerDefine parse(MethodInterceptorHolder holder) {

        Set<Annotation> a1 = AnnotatedElementUtils.findAllMergedAnnotations(holder.getMethod(), annotations);
        Set<Annotation> a2 = AnnotatedElementUtils.findAllMergedAnnotations(ClassUtils.getUserClass(holder.getTarget()), annotations);

        LoggerDefine define = new LoggerDefine();

        Stream.concat(a1.stream(), a2.stream())
                .forEach(ann -> {
                    if (ann instanceof ResourceAction) {
                        define.setAction(((ResourceAction) ann).name());
                    }
                    if (ann instanceof Resource) {
                        define.setDescribe(((Resource) ann).name());
                    }
                });

        return define;
    }
}
