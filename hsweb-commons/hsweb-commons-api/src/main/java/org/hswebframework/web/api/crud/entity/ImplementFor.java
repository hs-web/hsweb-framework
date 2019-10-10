package org.hswebframework.web.api.crud.entity;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ImplementFor {

    Class value();

    Class idType() default Void.class;
}
