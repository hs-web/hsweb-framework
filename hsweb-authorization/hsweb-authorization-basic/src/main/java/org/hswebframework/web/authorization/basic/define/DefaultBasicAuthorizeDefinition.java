package org.hswebframework.web.authorization.basic.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.*;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认权限权限定义
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DefaultBasicAuthorizeDefinition implements AopAuthorizeDefinition {

    @JsonIgnore
    private Class targetClass;


    @JsonIgnore
    private Method targetMethod;

    private ResourcesDefinition resources = new ResourcesDefinition();
    private DimensionsDefinition dimensions = new DimensionsDefinition();

    private String message;

    private Phased phased;

    @Override
    public boolean isEmpty() {
        return resources.getResources().isEmpty() && dimensions.getDimensions().isEmpty();
    }

    private static final Set<Class<? extends Annotation>> types = new HashSet<>(Arrays.asList(
            Authorize.class,
            DataAccess.class,
            Dimension.class,
            Resource.class,
            ResourceAction.class,
            DataAccessType.class
    ));

    public static AopAuthorizeDefinition from(Class targetClass, Method method) {
        DefaultBasicAuthorizeDefinition definition = new DefaultBasicAuthorizeDefinition();
        definition.setTargetClass(targetClass);
        definition.setTargetMethod(method);

        Set<Annotation> annotations = AnnotatedElementUtils.findAllMergedAnnotations(method, types);

        Set<Annotation> classAnnotation = AnnotatedElementUtils.findAllMergedAnnotations(targetClass, types);

        Map<Class, Annotation> classAnnotationMap = classAnnotation
                .stream()
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));

        Map<Class, Annotation> mapping = annotations
                .stream()
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));

        for (Annotation annotation : classAnnotation) {
            if (annotation instanceof Authorize) {
                definition.putAnnotation(((Authorize) annotation));
            }
            if (annotation instanceof Resource) {
                definition.putAnnotation(((Resource) annotation));
            }
        }

        for (Annotation annotation : annotations) {
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

        for (Annotation annotation : annotations) {

            if (annotation instanceof ResourceAction) {
                Optional.ofNullable(mapping.getOrDefault(Resource.class, classAnnotationMap.get(Resource.class)))
                        .map(Resource.class::cast)
                        .flatMap(res -> definition.getResources().getResource(res.id()))
                        .ifPresent(res -> {

                            ResourceAction ra = (ResourceAction) annotation;
                            ResourceActionDefinition action = definition.putAnnotation(res, ra);

                            Optional.ofNullable(mapping.get(DataAccessType.class))
                                    .map(DataAccessType.class::cast)
                                    .ifPresent(dat -> definition.putAnnotation(action, dat));
                        });
            }
            if (annotation instanceof DataAccess) {
                Optional.ofNullable(mapping.getOrDefault(Resource.class, classAnnotationMap.get(Resource.class)))
                        .map(Resource.class::cast)
                        .flatMap(res -> definition.getResources().getResource(res.id()))
                        .flatMap(res -> Optional.ofNullable(mapping.get(ResourceAction.class))
                                .map(ResourceAction.class::cast)
                                .flatMap(ra -> res.getAction(ra.id())))
                        .ifPresent(ra -> {
                            definition.putAnnotation(ra, (DataAccess) annotation);
                            Optional.ofNullable(mapping.get(DataAccessType.class))
                                    .map(DataAccessType.class::cast)
                                    .ifPresent(dat -> definition.putAnnotation(ra, dat));

                        });
            }

        }

        return definition;
    }

    public void putAnnotation(Authorize ann) {
        if (!ann.merge()) {
            getResources().getResources().clear();
            getDimensions().getDimensions().clear();
        }
        getResources().setPhased(ann.phased());
        for (Resource resource : ann.resources()) {
            putAnnotation(resource);
        }
        for (Dimension dimension : ann.dimension()) {
            putAnnotation(dimension);
        }
    }

    public void putAnnotation(Dimension ann) {
        if (ann.ignore()) {
            getDimensions().getDimensions().clear();
            return;
        }
        DimensionDefinition definition = new DimensionDefinition();
        definition.setTypeId(ann.type());
        definition.setDimensionId(new HashSet<>(Arrays.asList(ann.id())));
        definition.setLogical(ann.logical());
        getDimensions().addDimension(definition);
    }

    public void putAnnotation(Resource ann) {
        ResourceDefinition resource = new ResourceDefinition();
        resource.setId(ann.id());
        resource.setName(ann.name());
        resource.setLogical(ann.logical());
        resource.setDescription(String.join("\n", ann.description()));
        for (ResourceAction action : ann.actions()) {
            putAnnotation(resource, action);
        }
        resource.setGroup(new ArrayList<>(Arrays.asList(ann.group())));
        resources.addResource(resource, ann.merge());
    }

    public ResourceActionDefinition putAnnotation(ResourceDefinition definition, ResourceAction ann) {
        ResourceActionDefinition actionDefinition = new ResourceActionDefinition();
        actionDefinition.setId(ann.id());
        actionDefinition.setName(ann.name());
        actionDefinition.setDescription(String.join("\n", ann.description()));
        for (DataAccess dataAccess : ann.dataAccess()) {
            putAnnotation(actionDefinition, dataAccess);
        }
        definition.addAction(actionDefinition);
        return actionDefinition;
    }


    public void putAnnotation(ResourceActionDefinition definition, DataAccess ann) {
        if (ann.ignore()) {
            return;
        }
        DataAccessTypeDefinition typeDefinition = new DataAccessTypeDefinition();
        for (DataAccessType dataAccessType : ann.type()) {
            typeDefinition.setId(dataAccessType.id());
            typeDefinition.setName(dataAccessType.name());
            typeDefinition.setController(dataAccessType.controller());
            typeDefinition.setDescription(String.join("\n", dataAccessType.description()));
        }
        definition.getDataAccess()
                .getDataAccessTypes()
                .add(typeDefinition);
    }

    public void putAnnotation(ResourceActionDefinition definition, DataAccessType dataAccessType) {
        DataAccessTypeDefinition typeDefinition = new DataAccessTypeDefinition();
        typeDefinition.setId(dataAccessType.id());
        typeDefinition.setName(dataAccessType.name());
        typeDefinition.setController(dataAccessType.controller());
        typeDefinition.setDescription(String.join("\n", dataAccessType.description()));
        definition.getDataAccess()
                .getDataAccessTypes()
                .add(typeDefinition);
    }

}
