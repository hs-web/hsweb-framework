package org.hswebframework.web.loggin.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 使用AOP记录访问日志,并触发{@link AccessLoggerListener#onLogger(AccessLoggerInfo)}
 *
 * @author zhouhao
 * @since 3.0
 */
public class AopAccessLoggerSupport extends StaticMethodMatcherPointcutAdvisor {

    private final List<AccessLoggerListener> listeners = new ArrayList<>();

    public AopAccessLoggerSupport addListener(AccessLoggerListener loggerListener) {
        listeners.add(loggerListener);
        return this;
    }

    public AopAccessLoggerSupport() {
        setAdvice((MethodInterceptor) methodInvocation -> {
            MethodInterceptorHolder methodInterceptorHolder = MethodInterceptorHolder.create(methodInvocation);
            AccessLoggerInfo info = createLogger(methodInterceptorHolder);
            Object response;
            try {
                response = methodInvocation.proceed();
                info.setResponse(response);
                info.setResponseTime(System.currentTimeMillis());
            } catch (Throwable e) {
                info.setException(e);
                throw e;
            } finally {
                //触发监听
                listeners.forEach(listener -> listener.onLogger(info));
            }
            return response;
        });
    }

    protected AccessLoggerInfo createLogger(MethodInterceptorHolder holder) {
        AccessLoggerInfo info = new AccessLoggerInfo();
        info.setRequestTime(System.currentTimeMillis());

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

        info.setAction(action);
        info.setDescribe(describe);
        info.setParameters(holder.getArgs());
        info.setTarget(holder.getTarget().getClass());
        info.setMethod(holder.getMethod());

        HttpServletRequest request = WebUtil.getHttpServletRequest();
        if (null != request) {
            info.setHttpHeaders(WebUtil.getHeaders(request));
            info.setIp(WebUtil.getIpAddr(request));
            info.setHttpMethod(request.getMethod());
            info.setUrl(request.getRequestURL().toString());
        }
        return info;

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        AccessLogger ann = AopUtils.findAnnotation(aClass, method, AccessLogger.class);
        //注解了并且未取消
        return null != ann && !ann.ignore();
    }
}
