package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

/**
 * @see CreateAction
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ResourceAction {
    String id();

    String name();

    String[] description() default {};

    Logical logical() default Logical.DEFAULT;

    DataAccess[] dataAccess() default @DataAccess(ignore = true);
}
