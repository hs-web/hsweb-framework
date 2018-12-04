package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TwoFactor {
    String value();

    long timeout() default 10 * 60 * 1000L;

    String provider() default "totp";

    String parameter() default "verifyCode";

    boolean ignore() default false;
}
