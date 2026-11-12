package org.hswebframework.web.datasource.annotation;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface UseDefaultDataSource {
}
