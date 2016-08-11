package org.hsweb.web.core.logger.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLogger {
    String value();

    String[] describe() default "";
}
