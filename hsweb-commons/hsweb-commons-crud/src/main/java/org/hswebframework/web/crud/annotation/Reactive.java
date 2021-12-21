package org.hswebframework.web.crud.annotation;

import java.lang.annotation.*;

/**
 * 在实体类上注解,标记是否开启响应式仓库
 *
 * @author zhouhao
 * @see org.hswebframework.ezorm.rdb.mapping.ReactiveRepository
 * @since 4.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Reactive {
    boolean enable() default true;
}
