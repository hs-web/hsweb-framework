package org.hswebframework.web.service.form.simple.validator;

import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface JSR303AnnotationParserStrategy {

    boolean support(String type);

    JSR303AnnotationInfo parse(Map<String, Object> configMap);
}
