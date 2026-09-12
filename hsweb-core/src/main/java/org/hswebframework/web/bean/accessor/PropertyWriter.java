package org.hswebframework.web.bean.accessor;

import reactor.function.Consumer3;

import java.util.function.BiConsumer;

/**
 * 属性修改接口,用于修改一个对象中的属性
 *
 * @author zhouhao
 * @since 5.0.1
 */
public interface PropertyWriter extends BiConsumer<Object, Object> {
    @Override
    void accept(Object o, Object o2);
}
