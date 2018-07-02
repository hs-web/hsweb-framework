package org.hswebframework.web.service.form.simple.validator;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class JSR303AnnotationInfo {
    private Class<? extends java.lang.annotation.Annotation> annotation;

    private Map<String, Object> properties;
}