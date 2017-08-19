package org.hswebframework.web.authorization.basic.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 */
public class AopAuthorizingController extends StaticMethodMatcherPointcutAdvisor {

    public AopAuthorizingController(AuthorizingHandler authorizingHandler, AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {
        super((MethodInterceptor) methodInvocation -> {

            MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);

            MethodInterceptorContext paramContext = holder.createParamContext();

            AuthorizeDefinition definition = aopMethodAuthorizeDefinitionParser.parse(paramContext);

            if (null != definition) {
                Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);

                if (!definition.isEmpty()) {
                    AuthorizingContext context = new AuthorizingContext();
                    context.setAuthentication(authentication);
                    context.setDefinition(definition);
                    context.setParamContext(paramContext);
                    authorizingHandler.handle(context);
                }
            }
            return methodInvocation.proceed();
        });
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        //对controller进行控制
        return AopUtils.findAnnotation(aClass, Controller.class) != null
                || AopUtils.findAnnotation(aClass, RestController.class) != null;
    }
}
