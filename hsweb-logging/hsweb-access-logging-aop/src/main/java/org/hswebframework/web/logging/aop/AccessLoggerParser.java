package org.hswebframework.web.logging.aop;


import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.logging.LoggerDefine;

import java.lang.reflect.Method;

public interface AccessLoggerParser {
    boolean support(Class clazz, Method method);

    LoggerDefine parse(MethodInterceptorHolder holder);
}
