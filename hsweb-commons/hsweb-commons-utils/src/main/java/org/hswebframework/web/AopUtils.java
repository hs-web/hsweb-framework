/*
 * Copyright 2019 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public final class AopUtils {

    private AopUtils() {
    }

    public static <T extends Annotation> T findMethodAnnotation(Class targetClass, Method method, Class<T> annClass) {
        Method m = method;
        T a = AnnotationUtils.findAnnotation(m, annClass);
        if (a != null) {
            return a;
        }
        m = ClassUtils.getMostSpecificMethod(m, targetClass);
        a = AnnotationUtils.findAnnotation(m, annClass);
        if (a == null) {
            List<Class> supers = new ArrayList<>();
            supers.addAll(Arrays.asList(targetClass.getInterfaces()));
            if (targetClass.getSuperclass() != Object.class) {
                supers.add(targetClass.getSuperclass());
            }

            for (Class aClass : supers) {
                if(aClass==null){
                    continue;
                }
                Method ims[] = new Method[1];

                ReflectionUtils.doWithMethods(aClass, im -> {
                    if (im.getName().equals(method.getName()) && im.getParameterCount() == method.getParameterCount()) {
                        ims[0] = im;
                    }
                });

                if (ims[0] != null) {
                    a = findMethodAnnotation(aClass, ims[0], annClass);
                    if (a != null) {
                        return a;
                    }
                }
            }
        }
        return a;
    }

    public static <T extends Annotation> T findAnnotation(Class targetClass, Class<T> annClass) {
        return AnnotationUtils.findAnnotation(targetClass, annClass);
    }

    public static <T extends Annotation> T findAnnotation(Class targetClass, Method method, Class<T> annClass) {
        T a = findMethodAnnotation(targetClass, method, annClass);
        if (a != null) {
            return a;
        }
        return findAnnotation(targetClass, annClass);
    }

    public static <T extends Annotation> T findAnnotation(JoinPoint pjp, Class<T> annClass) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method m = signature.getMethod();
        Class<?> targetClass = pjp.getTarget().getClass();
        return findAnnotation(targetClass, m, annClass);
    }

    public static String getMethodBody(JoinPoint pjp) {
        StringBuilder methodName = new StringBuilder(pjp.getSignature().getName()).append("(");
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Class[] args = signature.getParameterTypes();
        for (int i = 0, len = args.length; i < len; i++) {
            if (i != 0) {
                methodName.append(",");
            }
            methodName.append(args[i].getSimpleName()).append(" ").append(names[i]);
        }
        return methodName.append(")").toString();
    }

    public static Map<String, Object> getArgsMap(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Map<String, Object> args = new LinkedHashMap<>();
        String names[] = signature.getParameterNames();
        for (int i = 0, len = names.length; i < len; i++) {
            args.put(names[i], pjp.getArgs()[i]);
        }
        return args;
    }
}