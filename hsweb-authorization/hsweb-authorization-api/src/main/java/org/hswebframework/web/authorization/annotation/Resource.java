package org.hswebframework.web.authorization.annotation;


import org.hswebframework.web.authorization.define.Phased;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Resource {
    String id();

    String name();

    ResourceAction[] actions() default {};

    Logical logical() default Logical.DEFAULT;

    Phased phased() default Phased.before;

    String[] description() default {};

    String[] group() default {};

    boolean merge() default true;
}
