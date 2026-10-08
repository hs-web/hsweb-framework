package org.hswebframework.web.bean.accessor;

/**
 * 属性级 transfer，用于直接将 source 属性复制到 target 属性。
 *
 * @author zhouhao
 * @since 5.0.2
 */
@FunctionalInterface
public interface PropertyTransfer {

    void transfer(Object source, Object target);
}
