package org.hswebframework.web.eventbus.annotation;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Subscribe {
    EventMode mode() default EventMode.SYNC;

    boolean transaction() default true;

    int priority() default Integer.MIN_VALUE;

}
