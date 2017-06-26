package org.hswebframework.web.commons.entity.factory;

/**
 * 属性复制接口,用于自定义属性复制
 *
 * @author zhouhao
 * @since 3.0
 */
public interface PropertyCopier<S, T> {
    T copyProperties(S source, T target);
}
