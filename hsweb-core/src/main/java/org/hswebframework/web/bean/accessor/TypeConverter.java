package org.hswebframework.web.bean.accessor;

import org.springframework.core.ResolvableType;

/**
 * 类型转换器，用于转换类型
 *
 * @author zhouhao
 * @since 5.0.1
 */
public interface TypeConverter {

    Object convert(Object source, ResolvableType type);

}
