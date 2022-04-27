package org.hswebframework.web.logging.aop;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logger.ReactiveLogger;
import org.hswebframework.web.logging.RequestInfo;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.logging.LoggerDefine;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.hswebframework.web.utils.FluxCache;
import org.hswebframework.web.utils.ReactiveWebUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    private final Map<Method, LoggerDefine> defineCache = new ConcurrentReferenceHashMap<>();

    private static final LoggerDefine UNSUPPORTED = new LoggerDefine();

    @SuppressWarnings("all")
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

    private Mono<RequestInfo> currentRequestInfo() {
        return Mono
                .subscriberContext()
                .handle((context, sink) -> {
                    if (context.hasKey(RequestInfo.class)) {
                        RequestInfo info = context.get(RequestInfo.class);
                        ReactiveLogger.log(context, ctx -> info.setContext(new HashMap<>(ctx)));
                        sink.next(info);
                    }
                });
    }

    protected Flux<?> wrapFluxResponse(Flux<?> flux, AccessLoggerInfo loggerInfo) {
        return Flux.deferWithContext(ctx -> this
                           .currentRequestInfo()
                           .doOnNext(loggerInfo::putAccessInfo)
                           .then(beforeRequest(loggerInfo))
                           .thenMany(flux)
                           .doOnError(loggerInfo::setException)
                           .doFinally(signal -> completeRequest(loggerInfo, ctx)))
                   .subscriberContext(ReactiveLogger.start("accessLogId", loggerInfo.getId()));
    }

    private Mono<Void> beforeRequest(AccessLoggerInfo loggerInfo) {

        AccessLoggerBeforeEvent event = new AccessLoggerBeforeEvent(loggerInfo);
        return Authentication
                .currentReactive()
                .flatMap(auth -> {
                    loggerInfo.putContext("userId", auth.getUser().getId());
                    loggerInfo.putContext("username", auth.getUser().getUsername());
                    loggerInfo.putContext("userName", auth.getUser().getName());
                    return ReactiveLogger
                            .mdc("userId", auth.getUser().getId(),
                                 "username", auth.getUser().getUsername(),
                                 "userName", auth.getUser().getName())
                            .thenReturn(auth);
                })
                .then(event.publish(eventPublisher));
    }

    private void completeRequest(AccessLoggerInfo loggerInfo, Context ctx) {
        loggerInfo.setResponseTime(System.currentTimeMillis());
        new AccessLoggerAfterEvent(loggerInfo)
                .publish(eventPublisher)
                .subscriberContext(ctx)
                .subscribe();
    }

    protected Mono<?> wrapMonoResponse(Mono<?> mono, AccessLoggerInfo loggerInfo) {
        return wrapFluxResponse(mono.flux(), loggerInfo)
                .singleOrEmpty();
    }

    private LoggerDefine createDefine(MethodInterceptorHolder holder) {
        return loggerParsers
                .stream()
                .filter(parser -> parser.support(ClassUtils.getUserClass(holder.getTarget()), holder.getMethod()))
                .findAny()
                .map(parser -> parser.parse(holder))
                .orElse(UNSUPPORTED);
    }

    @SuppressWarnings("all")
    protected AccessLoggerInfo createLogger(MethodInterceptorHolder holder) {
        AccessLoggerInfo info = new AccessLoggerInfo();
        info.setId(IDGenerator.MD5.generate());
        info.setRequestTime(System.currentTimeMillis());

        LoggerDefine define = defineCache.computeIfAbsent(holder.getMethod(), method -> createDefine(holder));

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
                args[i] = ((Mono<?>) val)
                        .doOnNext(param -> {
                            value.put(name, param);
                        });
            } else if (val instanceof Flux) {
                List<Object> arr = new ArrayList<>();
                value.put(name, arr);
                args[i] = ((Flux<?>) val)
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
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        return loggerParsers
                .stream()
                .anyMatch(parser -> parser.support(aClass, method));
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
