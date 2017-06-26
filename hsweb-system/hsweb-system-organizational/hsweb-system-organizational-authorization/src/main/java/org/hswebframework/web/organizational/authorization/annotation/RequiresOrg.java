package org.hswebframework.web.organizational.authorization.annotation;

import org.hswebframework.web.authorization.annotation.Logical;

import java.lang.annotation.*;

/**
 * @author zhouhao
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresOrg {
    String[] value() default {};

    boolean hasChidren() default true;

    Logical logocal() default Logical.OR;

}
