package org.hswebframework.web.bean.accessor;

import org.hswebframework.web.bean.Converter;

/**
 * 带类型转换的属性级 transfer。
 *
 * @author zhouhao
 * @since 5.0.2
 */
@FunctionalInterface
public interface ConverterPropertyTransfer {

    void transfer(Object source, Object target, Converter converter);
}
