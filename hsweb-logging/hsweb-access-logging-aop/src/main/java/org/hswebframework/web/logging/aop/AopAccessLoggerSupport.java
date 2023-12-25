package org.hswebframework.web.logging.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.logging.LoggerDefine;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.hswebframework.web.utils.WebUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用AOP记录访问日志,并触发{@link AccessLoggerListener#onLogger(AccessLoggerInfo)}
 *
 * @author zhouhao
 * @since 3.0
 */
public class AopAccessLoggerSupport extends StaticMethodMatcherPointcutAdvisor {

    @Autowired(required = false)
    private final List<AccessLoggerParser> loggerParsers = new ArrayList<>();

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    public AopAccessLoggerSupport() {
        setAdvice((MethodInterceptor) methodInvocation -> {
            MethodInterceptorHolder methodInterceptorHolder = MethodInterceptorHolder.create(methodInvocation);
            AccessLoggerInfo info = createLogger(methodInterceptorHolder);
            Object response = null;
            try {
                eventPublisher.publishEvent(new AccessLoggerBeforeEvent(info));
                response = methodInvocation.proceed();
                info.setResponse(response);
            } catch (Throwable e) {
                info.setException(e);
                throw e;
            } finally {
                info.setResponseTime(System.currentTimeMillis());
                //触发监听
                eventPublisher.publishEvent(new AccessLoggerAfterEvent(info));
            }
            return response;
        });
    }

    protected AccessLoggerInfo createLogger(MethodInterceptorHolder holder) {
        AccessLoggerInfo info = new AccessLoggerInfo();
        info.setId(IDGenerator.MD5.generate());

        info.setRequestTime(System.currentTimeMillis());
        LoggerDefine define = loggerParsers.stream()
                .filter(parser -> parser.support(ClassUtils.getUserClass(holder.getTarget()), holder.getMethod()))
                .findAny()
                .map(parser -> parser.parse(holder))
                .orElse(null);

        if (define != null) {
            info.setAction(define.getAction());
            info.setDescribe(define.getDescribe());
        }
        info.setParameters(holder.getNamedArguments());
        info.setTarget(holder.getTarget().getClass());
        info.setMethod(holder.getMethod());

        HttpServletRequest request = WebUtils.getHttpServletRequest();
        if (null != request) {
            info.setHttpHeaders(WebUtils.getHeaders(request));
            info.setIp(WebUtils.getIpAddr(request));
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
        return loggerParsers.stream().anyMatch(parser -> parser.support(aClass, method));
    }
}
