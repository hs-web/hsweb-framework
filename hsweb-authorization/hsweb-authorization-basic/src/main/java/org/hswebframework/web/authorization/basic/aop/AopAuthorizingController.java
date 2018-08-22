package org.hswebframework.web.authorization.basic.aop;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinitionInitializedEvent;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @see AuthorizeDefinitionInitializedEvent
 */
@Slf4j
public class AopAuthorizingController extends StaticMethodMatcherPointcutAdvisor implements CommandLineRunner {

    private static final long serialVersionUID = 1154190623020670672L;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private DefaultAopMethodAuthorizeDefinitionParser defaultParser = new DefaultAopMethodAuthorizeDefinitionParser();

    private boolean autoParse = false;

    public void setAutoParse(boolean autoParse) {
        this.autoParse = autoParse;
    }

    public AopAuthorizingController(AuthorizingHandler authorizingHandler, AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {
        super((MethodInterceptor) methodInvocation -> {

            MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);

            MethodInterceptorContext paramContext = holder.createParamContext();

            AuthorizeDefinition definition = aopMethodAuthorizeDefinitionParser.parse(methodInvocation.getThis().getClass(), methodInvocation.getMethod(), paramContext);
            Object result = null;
            boolean isControl = false;
            if (null != definition) {
                Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);
                //空配置也进行权限控制
//                if (!definition.isEmpty()) {

                AuthorizingContext context = new AuthorizingContext();
                context.setAuthentication(authentication);
                context.setDefinition(definition);
                context.setParamContext(paramContext);
                isControl = true;

                Phased dataAccessPhased = null;
                if (definition.getDataAccessDefinition() != null) {
                    dataAccessPhased = definition.getDataAccessDefinition().getPhased();
                }
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
//                }
            }
            if (!isControl) {
                result = methodInvocation.proceed();
            }
            return result;
        });
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        boolean support = AopUtils.findAnnotation(aClass, Controller.class) != null
                || AopUtils.findAnnotation(aClass, RestController.class) != null
                || AopUtils.findAnnotation(aClass, method, Authorize.class) != null;

        if (support && autoParse) {
            defaultParser.parse(aClass, method);
        }
        return support;
    }

    @Override
    public void run(String... args) throws Exception {
        if (autoParse) {
            List<AuthorizeDefinition> definitions = defaultParser.getAllParsed()
                    .stream().filter(def -> !def.isEmpty()).collect(Collectors.toList());


            log.info("publish AuthorizeDefinitionInitializedEvent,definition size:{}", definitions.size());
            eventPublisher.publishEvent(new AuthorizeDefinitionInitializedEvent(definitions));

            defaultParser.destroy();
        }
    }
}
