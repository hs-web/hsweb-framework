package org.hsweb.web.message;

import java.lang.annotation.*;

/**
 * Created by zhouhao on 16-4-14.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Jsonp {
    String callback() default "param.callback";
}
