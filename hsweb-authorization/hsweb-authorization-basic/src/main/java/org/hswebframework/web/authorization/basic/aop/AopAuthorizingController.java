package org.hswebframework.web.authorization.basic.aop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hswebframework.web.aop.MethodInterceptorContext;
import org.hswebframework.web.aop.MethodInterceptorHolder;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.hswebframework.web.authorization.define.*;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.utils.AnnotationUtils;
import org.reactivestreams.Publisher;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @see AuthorizeDefinitionInitializedEvent
 */
@Slf4j
@SuppressWarnings("all")
public class AopAuthorizingController extends StaticMethodMatcherPointcutAdvisor
    implements CommandLineRunner, MethodInterceptor, Ordered, SmartInitializingSingleton {

    private static final long serialVersionUID = 1154190623020670672L;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthorizingHandler authorizingHandler;

    @Autowired
    private AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser;

//    private DefaultAopMethodAuthorizeDefinitionParser defaultParser = new DefaultAopMethodAuthorizeDefinitionParser();

    private boolean autoParse = false;

    public void setAutoParse(boolean autoParse) {
        this.autoParse = autoParse;
    }


    protected Publisher<?> handleReactive0(AuthorizeDefinition definition,
                                           MethodInterceptorHolder holder,
                                           AuthorizingContext context,
                                           Supplier<? extends Publisher<?>> invoker) {
        MethodInterceptorContext interceptorContext = holder.createParamContext(invoker.get());
        context.setParamContext(interceptorContext);
        return this
            .invokeReactive(
                Authentication
                    .currentReactive()
                    .switchIfEmpty(
                        context.getDefinition().allowAnonymous()
                            ? Mono.empty()
                            : Mono.error(UnAuthorizedException.NoStackTrace::new))
                    .flatMap(auth -> {
                        context.setAuthentication(auth);
                        //响应式不再支持数据权限控制
                        return authorizingHandler.handRBACAsync(context);
                    }),
                (Publisher<?>) interceptorContext.getInvokeResult());
    }

    private Publisher<?> invokeReactive(Mono<?> before, Publisher<?> source) {
        if (source instanceof Mono) {
            return before.then((Mono<Object>) source);
        }
        return before.thenMany(source);
    }

    private <T> T invokeReactive(MethodInvocation invocation) {
        if (Mono.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
            return (T) Mono.defer(() -> doProceed(invocation));
        }
        if (Flux.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
            return (T) Flux.defer(() -> doProceed(invocation));
        }
        return doProceed(invocation);
    }

    @SneakyThrows
    private <T> T doProceed(MethodInvocation invocation) {

        return (T) invocation.proceed();
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);

        MethodInterceptorContext paramContext = holder.createParamContext();

        AuthorizeDefinition definition = aopMethodAuthorizeDefinitionParser
            .parse(methodInvocation.getThis().getClass(),
                   methodInvocation.getMethod(),
                   paramContext);
        Object result = null;
        boolean isControl = false;
        if (null != definition && !definition.isEmpty()) {
            AuthorizingContext context = new AuthorizingContext();
            context.setDefinition(definition);
            context.setParamContext(paramContext);

            Class<?> returnType = methodInvocation.getMethod().getReturnType();
            //handle reactive method
            if (Publisher.class.isAssignableFrom(returnType)) {
                return handleReactive0(definition, holder, context, () -> invokeReactive(methodInvocation));
            }

            Authentication authentication = Authentication
                .current()
                .orElse(null);

            if (authentication == null) {
                // 允许匿名访问
                if (definition.allowAnonymous()) {
                    return methodInvocation.proceed();
                }
                return new UnAuthorizedException.NoStackTrace();
            }

            context.setAuthentication(authentication);
            isControl = true;

            if (definition.getPhased() == Phased.before) {
                authorizingHandler.handRBAC(context);
                result = methodInvocation.proceed();
            } else {
                result = methodInvocation.proceed();
                context.setParamContext(holder.createParamContext(result));
                authorizingHandler.handRBAC(context);
            }
        }
        if (!isControl) {
            result = methodInvocation.proceed();
        }
        return result;

    }

    public AopAuthorizingController(AuthorizingHandler authorizingHandler, AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {
        this.authorizingHandler = authorizingHandler;
        this.aopMethodAuthorizeDefinitionParser = aopMethodAuthorizeDefinitionParser;
        setAdvice(this);
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        Authorize authorize;
        boolean support = AnnotationUtils.findAnnotation(aClass, Controller.class) != null
            || AnnotationUtils.findAnnotation(aClass, RestController.class) != null
            || AnnotationUtils.findAnnotation(aClass, RequestMapping.class) != null
            || ((authorize = AnnotationUtils.findAnnotation(aClass, method, Authorize.class)) != null && !authorize.ignore()
        );

        if (support && autoParse) {
            aopMethodAuthorizeDefinitionParser.parse(aClass, method);
        }
        return support;
    }

    @Override
    public void run(String... args) throws Exception {
//        if (autoParse) {
//            List<AuthorizeDefinition> definitions = aopMethodAuthorizeDefinitionParser
//                .getAllParsed()
//                .stream()
//                .filter(def -> !def.isEmpty())
//                .collect(Collectors.toList());
//            log.info("publish AuthorizeDefinitionInitializedEvent,definition size:{}", definitions.size());
//            eventPublisher.publishEvent(new AuthorizeDefinitionInitializedEvent(definitions));
//
//            //  defaultParser.destroy();
//        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (autoParse) {
            List<AuthorizeDefinition> definitions = aopMethodAuthorizeDefinitionParser
                .getAllParsed()
                .stream()
                .filter(def -> !def.isEmpty())
                .collect(Collectors.toList());
            log.info("publish AuthorizeDefinitionInitializedEvent,definition size:{}", definitions.size());
            eventPublisher.publishEvent(new AuthorizeDefinitionInitializedEvent(definitions));

            //  defaultParser.destroy();
        }
    }
}
