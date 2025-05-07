package org.hswebframework.web.logging.aop;

import com.google.common.collect.Maps;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logging.*;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.hswebframework.web.utils.WebUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
            Object response;
            try {
                AccessLoggerHolder.set(info);
                new AccessLoggerBeforeEvent(info)
                    .publish(eventPublisher)
                    .block();
                response = methodInvocation.proceed();
                info.setResponse(response);
            } catch (Throwable e) {
                info.setException(e);
                throw e;
            } finally {
                info.setResponseTime(System.currentTimeMillis());
                //触发监听
                new AccessLoggerAfterEvent(info)
                    .publish(eventPublisher)
                    .block();
                AccessLoggerHolder.remove();
            }
            return response;
        });
    }

    protected AccessLoggerInfo createLogger(MethodInterceptorHolder holder) {
        AccessLoggerInfo info = new AccessLoggerInfo();
        info.setId(IDGenerator.MD5.generate());

        info.setRequestTime(System.currentTimeMillis());
        LoggerDefine define = loggerParsers
            .stream()
            .filter(parser -> parser.support(ClassUtils.getUserClass(holder.getTarget()), holder.getMethod()))
            .findAny()
            .map(parser -> parser.parse(holder))
            .orElse(null);

        if (define != null) {
            info.setAction(define.getAction());
            info.setDescribe(define.getDescribe());
        }
        info.setParameters(parseParameter(holder));
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

    private Map<String, Object> parseParameter(MethodInterceptorHolder holder) {
        Predicate<String> ignoreParameter = loggerParsers
            .stream()
            .map(l -> l.ignoreParameter(holder))
            .reduce(Predicate::or)
            .orElseGet(() -> p -> false);

        return Maps.filterKeys(holder.getNamedArguments(), ignoreParameter::test);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean matches(@Nonnull Method method,@Nonnull Class<?> aClass) {
        if (null == AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class)) {
            return false;
        }
        AccessLogger ann = AnnotationUtils.findAnnotation(method, AccessLogger.class);
        if (ann != null && ann.ignore()) {
            return false;
        }
        return loggerParsers.stream().anyMatch(parser -> parser.support(aClass, method));
    }
}
