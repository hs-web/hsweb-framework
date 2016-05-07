package org.hsweb.web.core.logger.annotation;

import java.lang.annotation.*;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLogger {
    String value();
}
