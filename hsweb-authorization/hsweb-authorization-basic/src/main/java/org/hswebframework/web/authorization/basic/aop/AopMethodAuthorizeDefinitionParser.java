package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;

/**
 * 权限控制定义解析器,用于解析被拦截的请求是否需要进行权限控制,以及权限控制的方式
 *
 * @author zhouhao
 * @see AuthorizeDefinition
 */
public interface AopMethodAuthorizeDefinitionParser {

    /**
     * 解析权限控制定义
     *
     * @param paramContext 被拦截的方法上下文
     * @return 权限控制定义, 如果不进行权限控制则返回{@code null}
     */
    AuthorizeDefinition parse(MethodInterceptorContext paramContext);
}
