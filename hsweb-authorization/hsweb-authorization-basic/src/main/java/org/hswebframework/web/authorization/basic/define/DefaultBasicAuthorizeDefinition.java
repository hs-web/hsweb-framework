package org.hswebframework.web.authorization.basic.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.*;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Class<?> targetClass;


    @JsonIgnore
    private Method targetMethod;

    private ResourcesDefinition resources = new ResourcesDefinition();
    private DimensionsDefinition dimensions = new DimensionsDefinition();

    private String message;

    private Phased phased;

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static final Set<Class<? extends Annotation>> types = new HashSet<>(Arrays.asList(
            Authorize.class,
            DataAccess.class,
            Dimension.class,
            Resource.class,
            ResourceAction.class,
            DataAccessType.class
    ));

    public static AopAuthorizeDefinition from(Class<?> targetClass, Method method) {
        return new AopAuthorizeDefinitionParser(targetClass,method).parse();
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
        resource.setPhased(ann.phased());
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
            if(dataAccessType.ignore()){
                continue;
            }
            typeDefinition.setId(dataAccessType.id());
            typeDefinition.setName(dataAccessType.name());
            typeDefinition.setController(dataAccessType.controller());
            typeDefinition.setConfiguration(dataAccessType.configuration());
            typeDefinition.setDescription(String.join("\n", dataAccessType.description()));
        }
        if(StringUtils.isEmpty(typeDefinition.getId())){
            return;
        }
        definition.getDataAccess()
                .getDataAccessTypes()
                .add(typeDefinition);
    }

    public void putAnnotation(ResourceActionDefinition definition, DataAccessType dataAccessType) {
        if(dataAccessType.ignore()){
            return;
        }
        DataAccessTypeDefinition typeDefinition = new DataAccessTypeDefinition();
        typeDefinition.setId(dataAccessType.id());
        typeDefinition.setName(dataAccessType.name());
        typeDefinition.setController(dataAccessType.controller());
        typeDefinition.setConfiguration(dataAccessType.configuration());
        typeDefinition.setDescription(String.join("\n", dataAccessType.description()));
        definition.getDataAccess()
                .getDataAccessTypes()
                .add(typeDefinition);
    }

}
