package org.hswebframework.web.authorization.cloud;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用权限认证客户端
 * @author zhouhao
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AuthorizationServerAutoConfiguration.class)
public @interface EnableAuthorizationClient {

    Type value() default Type.Auto;

    enum Type {
        Auto, Feign
    }
}
