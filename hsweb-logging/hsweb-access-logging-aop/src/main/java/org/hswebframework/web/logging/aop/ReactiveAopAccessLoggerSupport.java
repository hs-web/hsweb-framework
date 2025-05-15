package org.hswebframework.web.logging.aop;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logger.ReactiveLogger;
import org.hswebframework.web.logging.*;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.hswebframework.web.utils.ReactiveWebUtils;
import org.reactivestreams.Publisher;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import jakarta.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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

    private final Map<CacheKey, LoggerDefine> defineCache = new ConcurrentReferenceHashMap<>();
    private final Map<CacheKey, Predicate<String>> ignoreParameterCache = new ConcurrentReferenceHashMap<>();

    private static final LoggerDefine UNSUPPORTED = new LoggerDefine();

    @SuppressWarnings("all")
    public ReactiveAopAccessLoggerSupport() {
        setAdvice((MethodInterceptor) methodInvocation -> {
            MethodInterceptorHolder methodInterceptorHolder = MethodInterceptorHolder.create(methodInvocation);
            AccessLoggerInfo info = createLogger(methodInterceptorHolder);
            Object response = methodInvocation.proceed();
            if (response instanceof Mono) {
                return wrapMonoResponse(((Mono<?>) response), info)
                        .contextWrite(Context.of(AccessLoggerInfo.class, info));
            } else if (response instanceof Flux) {
                return wrapFluxResponse(((Flux<?>) response), info)
                        .contextWrite(Context.of(AccessLoggerInfo.class, info));
            }
            return response;
        });
    }

    private Mono<RequestInfo> currentRequestInfo(ContextView context) {
        if (context.hasKey(RequestInfo.class)) {
            RequestInfo info = context.get(RequestInfo.class);
            ReactiveLogger.log(context, ctx -> info.setContext(new HashMap<>(ctx)));
            return Mono.just(info);
        }
        return Mono.empty();
    }

    protected Flux<?> wrapFluxResponse(Flux<?> flux, AccessLoggerInfo loggerInfo) {
        return Flux.deferContextual(ctx -> this
                .currentRequestInfo(ctx)
                .doOnNext(loggerInfo::putAccessInfo)
                .then(beforeRequest(loggerInfo))
                .thenMany(flux)
                .doOnError(loggerInfo::setException)
                .doFinally(signal -> completeRequest(loggerInfo, ctx)));
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
                .then(Mono.defer(() -> event.publish(eventPublisher)));
    }

    private void completeRequest(AccessLoggerInfo loggerInfo, ContextView ctx) {
        loggerInfo.setResponseTime(System.currentTimeMillis());
        new AccessLoggerAfterEvent(loggerInfo)
                .publish(eventPublisher)
                .contextWrite(ctx)
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

    private Predicate<String> ignoreParameter(MethodInterceptorHolder holder) {
        return loggerParsers
                .stream()
                .map(l -> l.ignoreParameter(holder))
                .reduce(Predicate::or)
                .orElseGet(() -> p -> false);
    }

    @SuppressWarnings("all")
    protected AccessLoggerInfo createLogger(MethodInterceptorHolder holder) {
        AccessLoggerInfo info = new AccessLoggerInfo();
        info.setId(IDGenerator.RANDOM.generate());
        info.setRequestTime(System.currentTimeMillis());

        LoggerDefine define = defineCache.computeIfAbsent(new CacheKey(
                ClassUtils.getUserClass(holder.getTarget()),
                holder.getMethod()), method -> createDefine(holder));

        if (define != null) {
            info.setAction(define.getAction());
            info.setDescribe(define.getDescribe());
        }

        info.setParameters(parseParameter(holder));
        info.setTarget(holder.getTarget().getClass());
        info.setMethod(holder.getMethod());
        return info;

    }

    private Map<String, Object> parseParameter(MethodInterceptorHolder holder) {
        Predicate<String> ignoreParameter = ignoreParameterCache.computeIfAbsent(new CacheKey(
                ClassUtils.getUserClass(holder.getTarget()),
                holder.getMethod()), method -> ignoreParameter(holder));

        Map<String, Object> value = new ConcurrentHashMap<>();

        String[] names = holder.getArgumentsNames();

        Object[] args = holder.getArguments();

        for (int i = 0; i < args.length; i++) {
            String name = names[i];
            if (ignoreParameter.test(name)) {
                continue;
            }
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
        return value;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean matches(@Nonnull Method method, @Nonnull Class<?> aClass) {
        //仅支持响应式
        if (!Publisher.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        // 只记录API请求
        if(null == AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class)){
            return false;
        }
        AccessLogger ann = AnnotationUtils.findAnnotation(method, AccessLogger.class);
        if (ann != null && ann.ignore()) {
            return false;
        }
        return loggerParsers
                .stream()
                .anyMatch(parser -> parser.support(aClass, method));
    }

    @Override
    @Nonnull
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        return chain
                .filter(exchange)
                .contextWrite(Context.of(RequestInfo.class, createAccessInfo(exchange)));
    }

    private RequestInfo createAccessInfo(ServerWebExchange exchange) {
        RequestInfo info = new RequestInfo();
        ServerHttpRequest request = exchange.getRequest();
        info.setRequestId(request.getId());
        info.setPath(request.getPath().value());
        info.setRequestMethod(request.getMethod().name());
        info.setHeaders(request.getHeaders().toSingleValueMap());

        Optional.ofNullable(ReactiveWebUtils.getIpAddr(request))
                .ifPresent(info::setIpAddr);

        return info;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class CacheKey {
        private Class<?> type;
        private Method method;
    }

}
