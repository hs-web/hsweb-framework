package org.hswebframework.web.bean.accessor;

public interface BeanAccessor {

    Object get(String property);

    void set(String property, Object value);

}
