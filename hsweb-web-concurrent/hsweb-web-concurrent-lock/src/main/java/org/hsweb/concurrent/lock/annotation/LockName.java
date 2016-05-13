package org.hsweb.concurrent.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhouhao on 16-5-13.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockName {
    String value();

    boolean isExpression() default false;

    String expressionLanguage() default "spel";
}
