package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;

/**
 * 权限控制上下文
 */
public class AuthorizingContext {
    private AuthorizeDefinition definition;

    private Authentication authentication;

    private MethodInterceptorParamContext paramContext;


    public AuthorizeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(AuthorizeDefinition definition) {
        this.definition = definition;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public MethodInterceptorParamContext getParamContext() {
        return paramContext;
    }

    public void setParamContext(MethodInterceptorParamContext paramContext) {
        this.paramContext = paramContext;
    }
}
