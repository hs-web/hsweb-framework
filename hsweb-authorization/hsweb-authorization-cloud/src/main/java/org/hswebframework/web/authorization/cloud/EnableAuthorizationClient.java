package org.hswebframework.web.authorization.cloud;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
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
