package org.hswebframework.web.loggin.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logger.ReactiveLogger;
import org.hswebframework.web.logging.RequestInfo;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.logging.LoggerDefine;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.hswebframework.web.utils.ReactiveWebUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ClassUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用AOP记录访问日志,并触发{@link AccessLoggerListener#onLogger(AccessLoggerInfo)}
 *
 * @author zhouhao
 * @since 3.0
 */
public class ReactiveAopAccessLoggerSupport extends StaticMethodMatcherPointcutAdvisor implements WebFilter {

    @Autowired(required = false)
    private final List<AccessLoggerParser> loggerParsers = new ArrayList<>();

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public ReactiveAopAccessLoggerSupport() {
        setAdvice((MethodInterceptor) methodInvocation -> {
            MethodInterceptorHolder methodInterceptorHolder = MethodInterceptorHolder.create(methodInvocation);
            AccessLoggerInfo info = createLogger(methodInterceptorHolder);
            Object response = methodInvocation.proceed();
            if (response instanceof Mono) {
                return wrapMonoResponse(((Mono<?>) response), info);
            } else if (response instanceof Flux) {
                return wrapFluxResponse(((Flux<?>) response), info);
            }
            return response;
        });
    }

    protected Flux<?> wrapFluxResponse(Flux<?> flux, AccessLoggerInfo loggerInfo) {
        return Mono.subscriberContext()
                .<RequestInfo>flatMap(ctx -> Mono.<RequestInfo>justOrEmpty(ctx.getOrEmpty(RequestInfo.class))
                        .doOnNext(info -> ReactiveLogger.log(ctx, info::setContext)))
                .doOnNext(loggerInfo::putAccessInfo)
                .thenMany(flux)
                .doOnError(loggerInfo::setException)
                .doFinally(f -> {
                    loggerInfo.setResponseTime(System.currentTimeMillis());
                    eventPublisher.publishEvent(new AccessLoggerAfterEvent(loggerInfo));
                }).subscriberContext(ReactiveLogger.start("accessLogId", loggerInfo.getId()));
    }

    protected Mono<?> wrapMonoResponse(Mono<?> mono, AccessLoggerInfo loggerInfo) {
        return Mono.subscriberContext()
                .<RequestInfo>flatMap(ctx -> Mono.<RequestInfo>justOrEmpty(ctx.getOrEmpty(RequestInfo.class))
                        .doOnNext(info -> ReactiveLogger.log(ctx, info::setContext)))
                .doOnNext(loggerInfo::putAccessInfo)
                .then(mono)
                .doOnError(loggerInfo::setException)
                .doOnSuccess(loggerInfo::setResponse)
                .doFinally(f -> {
                    loggerInfo.setResponseTime(System.currentTimeMillis());
                    eventPublisher.publishEvent(new AccessLoggerAfterEvent(loggerInfo));
                }).subscriberContext(ReactiveLogger.start("accessLogId", loggerInfo.getId()));
    }

    @SuppressWarnings("all")
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

        Map<String, Object> value = new ConcurrentHashMap<>();

        String[] names = holder.getArgumentsNames();

        Object[] args = holder.getArguments();

        for (int i = 0; i < args.length; i++) {
            String name = names[i];
            Object val = args[i];
            if (val == null) {
                value.put(name, "null");
                continue;
            }
            if (val instanceof Mono) {
                args[i] = ((Mono) val)
                        .doOnNext(param -> {
                            value.put(name, param);
                        });
            } else if (val instanceof Flux) {
                List<Object> arr = new ArrayList<>();
                value.put(name, arr);
                args[i] = ((Flux) val)
                        .doOnNext(param -> {
                            arr.add(param);
                        });
            } else {
                value.put(name, val);
            }
        }

        info.setParameters(value);
        info.setTarget(holder.getTarget().getClass());
        info.setMethod(holder.getMethod());
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain
                .filter(exchange)
                .subscriberContext(Context.of(RequestInfo.class, createAccessInfo(exchange)));
    }

    private RequestInfo createAccessInfo(ServerWebExchange exchange) {
        RequestInfo info = new RequestInfo();
        ServerHttpRequest request = exchange.getRequest();
        info.setRequestId(request.getId());
        info.setPath(request.getPath().value());
        info.setRequestMethod(request.getMethodValue());
        info.setHeaders(request.getHeaders().toSingleValueMap());

        Optional.ofNullable(ReactiveWebUtils.getIpAddr(request))
                .ifPresent(info::setIpAddr);

        return info;
    }
}
