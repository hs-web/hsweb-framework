package org.hswebframework.web.logging.aop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.logging.LoggerDefine;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class Swagger3AccessLoggerParser implements AccessLoggerParser {
    @Override
    public boolean support(Class clazz, Method method) {

        Tag api = AnnotationUtils.findAnnotation(clazz, Tag.class);
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);

        return api != null || operation != null;
    }

    @Override
    public LoggerDefine parse(MethodInterceptorHolder holder) {
        Tag api = holder.findAnnotation(Tag.class);
        Operation operation = holder.findAnnotation(Operation.class);
        String action = "";
        if (api != null) {
            action = action.concat(api.name());
        }
        if (null != operation) {
            action = StringUtils.isEmpty(action) ? operation.summary() : action + "-" + operation.summary();
        }
        return new LoggerDefine(action, "");
    }
}
