package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.hswebframework.web.authorization.define.ResourceActionDefinition;
import org.hswebframework.web.authorization.define.ResourceDefinition;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AopAuthorizeDefinitionParser {

    private static final Set<Class<? extends Annotation>> types = new HashSet<>(Arrays.asList(
            Authorize.class,
            DataAccess.class,
            Dimension.class,
            Resource.class,
            ResourceAction.class,
            DataAccessType.class
    ));

    private Set<Annotation> methodAnnotation;

    private Set<Annotation> classAnnotation;

    private Map<Class<? extends Annotation>, List<Annotation>> classAnnotationGroup;

    private Map<Class<? extends Annotation>, List<Annotation>> methodAnnotationGroup;

    private DefaultBasicAuthorizeDefinition definition;

    AopAuthorizeDefinitionParser(Class<?> targetClass, Method method) {
        definition = new DefaultBasicAuthorizeDefinition();
        definition.setTargetClass(targetClass);
        definition.setTargetMethod(method);

        methodAnnotation = AnnotatedElementUtils.findAllMergedAnnotations(method, types);

        classAnnotation = AnnotatedElementUtils.findAllMergedAnnotations(targetClass, types);

        classAnnotationGroup = classAnnotation
                .stream()
                .collect(Collectors.groupingBy(Annotation::annotationType));

        methodAnnotationGroup = methodAnnotation
                .stream()
                .collect(Collectors.groupingBy(Annotation::annotationType));
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
        }
    }

    private void initClassDataAccessAnnotation(){
        for (Annotation annotation : classAnnotation) {
            if (annotation instanceof DataAccessType ||
                    annotation instanceof DataAccess) {
                for (ResourceDefinition resource : definition.getResources().getResources()) {
                    for (ResourceActionDefinition action : resource.getActions()) {
                        if (annotation instanceof DataAccessType) {
                            definition.putAnnotation(action, (DataAccessType) annotation);
                        } else {
                            definition.putAnnotation(action, (DataAccess) annotation);
                        }
                    }
                }
            }
        }
    }

    private void initMethodDataAccessAnnotation() {
        for (Annotation annotation : methodAnnotation) {

            if (annotation instanceof ResourceAction) {
                getAnnotationByType(Resource.class)
                        .map(res -> definition.getResources().getResource(res.id()).orElse(null))
                        .filter(Objects::nonNull)
                        .forEach(res -> {
                            ResourceAction ra = (ResourceAction) annotation;
                            ResourceActionDefinition action = definition.putAnnotation(res, ra);
                            getAnnotationByType(DataAccessType.class)
                                    .findFirst()
                                    .ifPresent(dat -> definition.putAnnotation(action, dat));
                        });
            }
            Optional<ResourceActionDefinition> actionDefinition = getAnnotationByType(Resource.class)
                    .map(res -> definition.getResources().getResource(res.id()).orElse(null))
                    .filter(Objects::nonNull)
                    .flatMap(res -> getAnnotationByType(ResourceAction.class)
                            .map(ra -> res.getAction(ra.id())
                                    .orElse(null))
                    )
                    .filter(Objects::nonNull)
                    .findFirst();

            if (annotation instanceof DataAccessType) {
                actionDefinition.ifPresent(ra -> definition.putAnnotation(ra, (DataAccessType) annotation));
            }

            if (annotation instanceof DataAccess) {
                actionDefinition.ifPresent(ra -> {
                    definition.putAnnotation(ra, (DataAccess) annotation);
                    getAnnotationByType(DataAccessType.class)
                            .findFirst()
                            .ifPresent(dat -> definition.putAnnotation(ra, dat));
                });
            }

        }
    }

     AopAuthorizeDefinition parse() {
        initClassAnnotation();
        initClassDataAccessAnnotation();
        initMethodAnnotation();
        initMethodDataAccessAnnotation();

        return definition;
    }


    private <T extends Annotation> Stream<T> getAnnotationByType(Class<T> type) {
        return Optional.ofNullable(methodAnnotationGroup.getOrDefault(type, classAnnotationGroup.get(type)))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(type::cast);
    }

}
