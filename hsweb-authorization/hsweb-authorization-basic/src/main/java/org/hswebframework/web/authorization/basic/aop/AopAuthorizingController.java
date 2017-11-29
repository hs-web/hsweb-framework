package org.hswebframework.web.authorization.basic.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.basic.handler.AuthorizingHandler;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 */
public class AopAuthorizingController extends StaticMethodMatcherPointcutAdvisor {

    private static final long serialVersionUID = 1154190623020670672L;

    public AopAuthorizingController(AuthorizingHandler authorizingHandler, AopMethodAuthorizeDefinitionParser aopMethodAuthorizeDefinitionParser) {
        super((MethodInterceptor) methodInvocation -> {

            MethodInterceptorHolder holder = MethodInterceptorHolder.create(methodInvocation);

            MethodInterceptorContext paramContext = holder.createParamContext();

            AuthorizeDefinition definition = aopMethodAuthorizeDefinitionParser.parse(paramContext);
            Object result = true;
            boolean isControl = false;
            if (null != definition) {
                Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);
                if (!definition.isEmpty()) {
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
                        authorizingHandler.handRDAC(context);

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

                        authorizingHandler.handRDAC(context);

                        //方法调用后验证数据权限
                        if (dataAccessPhased == Phased.after) {
                            authorizingHandler.handleDataAccess(context);
                        }
                    }
                }
            }
            if (!isControl) {
                result = methodInvocation.proceed();
            }

            return result;
        });
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        //对controller进行控制
        return AopUtils.findAnnotation(aClass, Controller.class) != null
                || AopUtils.findAnnotation(aClass, RestController.class) != null
                || AopUtils.findAnnotation(aClass, method, Authorize.class) != null;
    }
}
