package org.hswebframework.web.logging.aop;


import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.logging.LoggerDefine;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public interface AccessLoggerParser {
    boolean support(Class<?> clazz, Method method);

    LoggerDefine parse(MethodInterceptorHolder holder);

    /**
     * @param holder MethodInterceptorHolder
     * @return 是否忽略支持记录当前参数
     */
    default Predicate<String> ignoreParameter(MethodInterceptorHolder holder) {
        return p -> false;
    }
}
