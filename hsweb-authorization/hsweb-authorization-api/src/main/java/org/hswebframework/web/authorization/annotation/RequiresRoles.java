package org.hswebframework.web.authorization.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Dimension(type = "role", description = "控制角色")
public @interface RequiresRoles {

    @AliasFor(annotation = Dimension.class, attribute = "id")
    String[] value() default {};

    @AliasFor(annotation = Dimension.class, attribute = "logical")
    Logical logical() default Logical.DEFAULT;

}
