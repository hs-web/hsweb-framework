package org.hswebframework.web.authorization.basic.configuration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * 开启基于AOP的权限控制
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.Authentication
 * @see org.hswebframework.web.authorization.annotation.Authorize
 * @see org.hswebframework.web.authorization.annotation.Resource
 * @see org.hswebframework.web.authorization.annotation.ResourceAction
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration({AopAuthorizeAutoConfiguration.class})
public @interface EnableAopAuthorize {

}
