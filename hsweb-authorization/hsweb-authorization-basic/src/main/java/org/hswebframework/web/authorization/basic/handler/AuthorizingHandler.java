package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;

/**
 * aop方式权限控制处理器
 * @author zhouhao
 */
public interface AuthorizingHandler {
    void handle(AuthorizingContext context);
}
