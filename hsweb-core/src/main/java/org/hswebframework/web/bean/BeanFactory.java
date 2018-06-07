package org.hswebframework.web.bean;

public interface BeanFactory {

    <T> T newInstance(Class<T> beanType);
}
