package org.hsweb.web.datasource.dynamic;

import org.hsweb.web.core.datasource.DynamicDataSource;

import java.lang.annotation.*;

/**
 * 通过注解，切换数据源为默认数据源
 *
 * @author zhouhao
 * @see DynamicDataSource#useDefault(boolean)
 * @since 2.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseDefaultDataSource {

    /**
     * 方法执行后是否切换到上次使用的数据源
     *
     * @return 是否自动切换为之前的数据源
     */
    boolean value() default true;
}
