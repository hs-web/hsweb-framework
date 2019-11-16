package org.hswebframework.web.authorization.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.aop.MethodInterceptorContext;
import org.hswebframework.web.authorization.Authentication;

/**
 * 权限控制上下文
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizingContext {
    private AuthorizeDefinition definition;

    private Authentication authentication;

    private MethodInterceptorContext paramContext;

}
