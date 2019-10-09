package org.hswebframework.web.crud.annotation;

import java.lang.annotation.*;

/**
 * @see org.hswebframework.ezorm.rdb.mapping.ReactiveRepository
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Reactive {
}
