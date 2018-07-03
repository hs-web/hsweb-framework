package org.hswebframework.web.service.form.simple.validator.jsr303;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.service.form.simple.validator.JSR303AnnotationInfo;
import org.hswebframework.web.service.form.simple.validator.JSR303AnnotationParserStrategy;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public abstract class AbstractStrategy implements JSR303AnnotationParserStrategy {

    private List<PropertyMapping> propertyMappings = new ArrayList<>();

    public AbstractStrategy() {
        propertyMappings.add(PropertyMapping.of("message", String.class));
    }

    public void addPropertyMapping(PropertyMapping mapping) {
        propertyMappings.add(mapping);
    }

    protected String getTypeString() {
        return getAnnotationType().getSimpleName();
    }

    protected abstract Class<? extends Annotation> getAnnotationType();

    @Getter
    @Setter
    public static class PropertyMapping<T> {
        private String   name;
        private Class<T> type;

        public static <T> PropertyMapping<T> of(String name, Class<T> type) {
            PropertyMapping mapping = new PropertyMapping<>();

            mapping.name = name;
            mapping.type = type;

            return mapping;
        }

        public static <T> PropertyMapping<T> of(String name, Class<T> type, Function<Object, T> converter) {
            PropertyMapping mapping = new PropertyMapping<>();
            mapping.name = name;
            mapping.type = type;
            mapping.converter = converter;
            return mapping;
        }

        private Function<Object, T> converter = source -> FastBeanCopier.DEFAULT_CONVERT.convert(source, type, null);
    }

    @Override
    public boolean support(String type) {
        return type != null && (getTypeString().equalsIgnoreCase(type));
    }

    @Override
    public JSR303AnnotationInfo parse(Map<String, Object> configMap) {
        JSR303AnnotationInfo info = new JSR303AnnotationInfo();
        info.setAnnotation(getAnnotationType());

        Map<String, Object> properties = new HashMap<>();

        propertyMappings.forEach(mapping -> {
            Object value = mapping.getConverter().apply(configMap.get(mapping.getName()));
            if (!StringUtils.isEmpty(value)) {
                properties.put(mapping.getName(), value);
            }
        });

        List<Object> groups = null;

        Object groupObject = new JSONObject(configMap).get("groups");
        if (groupObject instanceof JSONArray) {
            groups = ((JSONArray) groupObject);
        }
        if (groupObject instanceof String) {
            groups = Arrays.asList(((String) groupObject).split("[,]"));
        }

        if (!CollectionUtils.isEmpty(groups)) {
            properties.put("groups", groups.stream().map(obj -> {
                if ("create".equals(obj)) {
                    return CreateGroup.class;
                } else if ("update".equals(obj)) {
                    return UpdateGroup.class;
                } else {
                    try {
                        return Class.forName(String.valueOf(obj));
                    } catch (ClassNotFoundException e) {
                        return CreateGroup.class;
                    }
                }
            }).toArray());
        }
        info.setProperties(properties);
        return info;
    }
}
