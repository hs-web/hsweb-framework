package org.hswebframework.web.authorization.access;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ParamContext extends Serializable {

    Object getTarget();

    <T> Optional<T> getParameter(String name);

    <T extends Annotation> T getAnnotation();

    Map<String, Object> getParams();
}
