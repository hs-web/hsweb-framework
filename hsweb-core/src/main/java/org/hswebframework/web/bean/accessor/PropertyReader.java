package org.hswebframework.web.bean.accessor;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 属性读取接口,用于获取一个对象中的属性
 *
 * @author zhouhao
 * @since 5.0.1
 */
public interface PropertyReader extends Function<Object, Object> {


    Object apply(Object object);

}
