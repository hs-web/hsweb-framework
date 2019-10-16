package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Dimension {

    String type();

    String[] id() default {};

    Logical logical() default Logical.DEFAULT;

    String[] description() default {};

    boolean ignore() default false;
}