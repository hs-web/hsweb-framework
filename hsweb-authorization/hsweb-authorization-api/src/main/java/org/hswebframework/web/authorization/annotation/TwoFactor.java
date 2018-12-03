package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TwoFactor {
    String operation() default "";

    long timeout() default 10 * 60 * 1000L;

    String provider() default "totp";

    boolean ignore() default false;
}
