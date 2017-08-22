package org.hswebframework.web.authorization.basic.aop;

import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;

/**
 * 自定义权限控制定义，在拦截到方法后，优先使用此接口来获取权限控制方式
 * @see AuthorizeDefinition
 * @author zhouhao
 */
public interface AopMethodAuthorizeDefinitionCustomizerParser {
    AuthorizeDefinition parse(MethodInterceptorContext context);
}
