package org.hswebframework.web.commons.entity.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ImplementFor {

    Class value();

    Class idType() default Void.class;
}
