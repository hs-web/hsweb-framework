package org.hswebframework.web.loggin.aop;


import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.logging.LoggerDefine;

import java.lang.reflect.Method;

public interface AccessLoggerParser {
    boolean support(Class clazz, Method method);

    LoggerDefine parse(MethodInterceptorHolder holder);
}
