package org.hswebframework.web.bean.accessor;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface BeanAccessor {

    Object get(Object source, String property);

    void set(Object source, String property, Object value);
    Object copy(Object source, Object target, BiPredicate<String, Object> filter);
}
