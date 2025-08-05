package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.hswebframework.web.authorization.define.ResourceActionDefinition;
import org.hswebframework.web.authorization.define.ResourceDefinition;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AopAuthorizeDefinitionParser {

    private static final Set<Class<? extends Annotation>> types = new HashSet<>(Arrays.asList(
        Authorize.class,
        Dimension.class,
        Resource.class,
        ResourceAction.class,
        Dimensions.class
    ));

    private final Set<Annotation> methodAnnotation;

    private final Set<Annotation> classAnnotation;

    private final Map<Class<? extends Annotation>, List<Annotation>> classAnnotationGroup;

    private final Map<Class<? extends Annotation>, List<Annotation>> methodAnnotationGroup;

    private final DefaultBasicAuthorizeDefinition definition;

    AopAuthorizeDefinitionParser(Class<?> targetClass, Method method) {
        definition = new DefaultBasicAuthorizeDefinition();
        definition.setTargetClass(targetClass);
        definition.setTargetMethod(method);
        methodAnnotation = loadAnnotations(method);
        classAnnotation = loadAnnotations(targetClass);

        classAnnotationGroup = classAnnotation
            .stream()
            .collect(Collectors.groupingBy(Annotation::annotationType));

        methodAnnotationGroup = methodAnnotation
            .stream()
            .collect(Collectors.groupingBy(Annotation::annotationType));
    }

    private Set<Annotation> loadAnnotations(AnnotatedElement element) {
        return types
            .stream()
            .flatMap(s -> {
                if (s.isAnnotationPresent(Repeatable.class)) {
                    return AnnotatedElementUtils
                        .findMergedRepeatableAnnotations(element, s)
                        .stream();
                }
                return AnnotatedElementUtils
                    .findAllMergedAnnotations(element, s)
                    .stream();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private void initClassAnnotation() {
        for (Annotation annotation : classAnnotation) {
            if (annotation instanceof Authorize) {
                definition.putAnnotation(((Authorize) annotation));
            }
            if (annotation instanceof Resource) {
                definition.putAnnotation(((Resource) annotation));
            }
        }
    }

    private void initMethodAnnotation() {
        for (Annotation annotation : methodAnnotation) {
            if (annotation instanceof Authorize) {
                definition.putAnnotation(((Authorize) annotation));
            }
            if (annotation instanceof Resource) {
                definition.putAnnotation(((Resource) annotation));
            }
            if (annotation instanceof Dimension) {
                definition.putAnnotation(((Dimension) annotation));
            }
            if (annotation instanceof Dimensions) {
                definition.putAnnotation(((Dimensions) annotation));
            }

            if (annotation instanceof ResourceAction) {
                getAnnotationByType(Resource.class)
                    .map(res -> definition.getResources().getResource(res.id()).orElse(null))
                    .filter(Objects::nonNull)
                    .forEach(res -> {
                        definition.putAnnotation(res, (ResourceAction) annotation);
                    });
            }
        }
    }

    AopAuthorizeDefinition parse() {
        //没有任何注解
        if (CollectionUtils.isEmpty(classAnnotation) && CollectionUtils.isEmpty(methodAnnotation)) {
            return EmptyAuthorizeDefinition.instance;
        }
        initClassAnnotation();
        initMethodAnnotation();

        return definition;
    }


    private <T extends Annotation> Stream<T> getAnnotationByType(Class<T> type) {
        return Optional
            .ofNullable(methodAnnotationGroup.getOrDefault(type, classAnnotationGroup.get(type)))
            .stream()
            .flatMap(Collection::stream)
            .map(type::cast);
    }

}
