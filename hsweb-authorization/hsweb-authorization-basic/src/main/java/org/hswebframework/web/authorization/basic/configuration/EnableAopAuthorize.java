package org.hswebframework.web.authorization.basic.configuration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author zhouhao
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration({AopAuthorizeAutoConfiguration.class, AuthorizingHandlerAutoConfiguration.class})
public @interface EnableAopAuthorize {

}
