package org.hswebframework.web.datasource.annotation;

import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DynamicDataSource;

import java.lang.annotation.*;

/**
 * @author zhouhao
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface UseDataSource {
    /**
     * @return 数据源ID ,支持表达式如 : ${#param.id}
     * @see DynamicDataSource#getId()
     */
    String value();

    /**
     * @return 数据源不存在时, 是否使用默认数据源.
     * 如果为{@code false},当数据源不存在的时候,
     * 将抛出 {@link org.hswebframework.web.datasource.exception.DataSourceNotFoundException}
     * @see DataSourceHolder#currentExisting()
     */
    boolean fallbackDefault() default false;
}
