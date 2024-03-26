package org.hswebframework.web.logging.aop;


import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.logging.LoggerDefine;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DefaultAccessLoggerParser implements AccessLoggerParser {
    @Override
    public boolean support(Class<?> clazz, Method method) {
        AccessLogger ann = AnnotationUtils.findAnnotation(method, AccessLogger.class);
        //注解了并且未取消
        return null != ann && !ann.ignore();
    }

    @Override
    public LoggerDefine parse(MethodInterceptorHolder holder) {
        AccessLogger methodAnn = holder.findMethodAnnotation(AccessLogger.class);
        AccessLogger classAnn = holder.findClassAnnotation(AccessLogger.class);
        String action = Stream.of(classAnn, methodAnn)
                              .filter(Objects::nonNull)
                              .map(AccessLogger::value)
                              .reduce((c, m) -> c.concat("-").concat(m))
                              .orElse("");
        String describe = Stream.of(classAnn, methodAnn)
                                .filter(Objects::nonNull)
                                .map(AccessLogger::describe)
                                .flatMap(Stream::of)
                                .reduce((c, s) -> c.concat("\n").concat(s))
                                .orElse("");
        Set<String> ignoreParameter = Stream
                .of(classAnn, methodAnn)
                .filter(Objects::nonNull)
                .map(AccessLogger::ignoreParameter)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        return new LoggerDefine(action, describe);

    }

    @Override
    public Predicate<String> ignoreParameter(MethodInterceptorHolder holder) {
        AccessLogger methodAnn = holder.findMethodAnnotation(AccessLogger.class);
        AccessLogger classAnn = holder.findClassAnnotation(AccessLogger.class);
        Set<String> ignoreParameter = Stream
                .of(classAnn, methodAnn)
                .filter(Objects::nonNull)
                .map(AccessLogger::ignoreParameter)
                .flatMap(s-> Arrays.stream(s.split(",")))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        return parameter -> ignoreParameter.contains("*") || ignoreParameter.contains(parameter);
    }
}
