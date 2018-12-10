package org.hswebframework.web.concurrent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 开启限流
 *
 * @author zhouhao
 * @see org.hswebframework.web.concurrent.RateLimiter
 * @since 3.0.4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RateLimiter {

    /**
     * key,支持spel表达式: ${#username+'login'} .默认以当前注解的方法为key
     *
     * @return 限流Key
     */
    String[] key() default {};

    /**
     * @return 时间单位内允许访问次数, 如:每秒100次
     */
    double permits() default 100D;

    /**
     * @return 时间单位, 支持毫秒及以上的时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long acquire() default 10;

    TimeUnit acquireTimeUnit() default TimeUnit.SECONDS;
}
