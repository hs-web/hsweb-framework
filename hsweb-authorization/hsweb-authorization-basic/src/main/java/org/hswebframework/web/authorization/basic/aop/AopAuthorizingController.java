package org.hswebframework.web.authorization.basic.aop;

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
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @see AuthorizeDefinitionInitializedEvent
 */
@Slf4j
@SuppressWarnings("all")
public class AopAuthorizingController extends StaticMethodMatcherPointcutAdvisor implements CommandLineRunner, MethodInterceptor {

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

    protected Mono<?> handleReactive(AuthorizeDefinition definition, MethodInterceptorHolder holder, AuthorizingContext context, Mono<?> mono) {

        return Authentication.currentReactive()
                .switchIfEmpty(Mono.error(new UnAuthorizedException()))
                .flatMap(auth -> {
                    ResourcesDefinition resources = definition.getResources();

                    context.setAuthentication(auth);
                    if (definition.getPhased() == Phased.before) {
                        authorizingHandler.handRBAC(context);
                        if (resources != null && resources.getPhased() == Phased.before) {
                            authorizingHandler.handleDataAccess(context);
                        } else {
                            return mono.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handleDataAccess(context);
                            });
                        }
                    } else {
                        if (resources != null && resources.getPhased() == Phased.before) {
                            authorizingHandler.handleDataAccess(context);
                            return mono.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handRBAC(context);
                            });
                        } else {
                            return mono.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handle(context);
                            });
                        }

                    }
                    return mono;
                });
    }

    protected Flux<?> handleReactive(AuthorizeDefinition definition, MethodInterceptorHolder holder, AuthorizingContext context, Flux<?> flux) {

        return Authentication.currentReactive()
                .switchIfEmpty(Mono.error(new UnAuthorizedException()))
                .flatMapMany(auth -> {
                    ResourcesDefinition resources = definition.getResources();

                    context.setAuthentication(auth);
                    if (definition.getPhased() == Phased.before) {
                        authorizingHandler.handRBAC(context);
                        if (resources != null && resources.getPhased() == Phased.before) {
                            authorizingHandler.handleDataAccess(context);
                        } else {
                            return flux.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handleDataAccess(context);
                            });
                        }
                    } else {

                        if (resources != null && resources.getPhased() == Phased.before) {
                            authorizingHandler.handleDataAccess(context);
                            return flux.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handRBAC(context);
                            });
                        } else {
                            return flux.doOnNext(res -> {
                                context.setParamContext(holder.createParamContext(res));
                                authorizingHandler.handle(context);
                            });
                        }

                    }
                    return flux;
                });
    }


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);

        MethodInterceptorContext paramContext = holder.createParamContext();

        AuthorizeDefinition definition = aopMethodAuthorizeDefinitionParser.parse(methodInvocation.getThis().getClass(), methodInvocation.getMethod(), paramContext);
        Object result = null;
        boolean isControl = false;
        if (null != definition) {
            AuthorizingContext context = new AuthorizingContext();
            context.setDefinition(definition);
            context.setParamContext(paramContext);

            Class<?> returnType = methodInvocation.getMethod().getReturnType();
            //handle reactive method
            if (Mono.class.isAssignableFrom(returnType)) {
                return handleReactive(definition, holder, context, ((Mono<?>) methodInvocation.proceed()));
            } else if (Flux.class.isAssignableFrom(returnType)) {
                return handleReactive(definition, holder, context, ((Flux<?>) methodInvocation.proceed()));
            }

            Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);

            context.setAuthentication(authentication);
            isControl = true;

            Phased dataAccessPhased = null;
            dataAccessPhased = definition.getResources().getPhased();
            if (definition.getPhased() == Phased.before) {
                //RDAC before
                authorizingHandler.handRBAC(context);

                //方法调用前验证数据权限
                if (dataAccessPhased == Phased.before) {
                    authorizingHandler.handleDataAccess(context);
                }

                result = methodInvocation.proceed();

                //方法调用后验证数据权限
                if (dataAccessPhased == Phased.after) {
                    context.setParamContext(holder.createParamContext(result));
                    authorizingHandler.handleDataAccess(context);
                }
            } else {
                //方法调用前验证数据权限
                if (dataAccessPhased == Phased.before) {
                    authorizingHandler.handleDataAccess(context);
                }

                result = methodInvocation.proceed();
                context.setParamContext(holder.createParamContext(result));

                authorizingHandler.handRBAC(context);

                //方法调用后验证数据权限
                if (dataAccessPhased == Phased.after) {
                    authorizingHandler.handleDataAccess(context);
                }
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
        boolean support = AnnotationUtils.findAnnotation(aClass, Controller.class) != null
                || AnnotationUtils.findAnnotation(aClass, RestController.class) != null
                || AnnotationUtils.findAnnotation(aClass, method, Authorize.class) != null;

        if (support && autoParse) {
            aopMethodAuthorizeDefinitionParser.parse(aClass, method);
        }
        return support;
    }

    @Override
    public void run(String... args) throws Exception {
        if (autoParse) {
            List<AuthorizeDefinition> definitions = aopMethodAuthorizeDefinitionParser.getAllParsed()
                    .stream()
                    .filter(def -> !def.isEmpty())
                    .collect(Collectors.toList());
            log.info("publish AuthorizeDefinitionInitializedEvent,definition size:{}", definitions.size());
            eventPublisher.publishEvent(new AuthorizeDefinitionInitializedEvent(definitions));

          //  defaultParser.destroy();
        }
    }


}
