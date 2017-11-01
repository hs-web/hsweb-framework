package org.hswebframework.web.loggin.aop;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.logging.LoggerDefine;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class SwaggerAccessLoggerParser implements AccessLoggerParser {
    @Override
    public boolean support(Class clazz, Method method) {
        AccessLogger ann = AopUtils.findAnnotation(clazz, method, AccessLogger.class);
        if (null != ann && ann.ignore()) {
            return false;
        }

        Api api = AnnotationUtils.findAnnotation(clazz, Api.class);
        ApiOperation operation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

        return api != null || operation != null;
    }

    @Override
    public LoggerDefine parse(MethodInterceptorHolder holder) {
        ApiOperation operation = holder.findAnnotation(ApiOperation.class);
        String action = "";
        if (null != operation) {
            action = operation.value();
        }
        return new LoggerDefine(action, "");
    }
}
