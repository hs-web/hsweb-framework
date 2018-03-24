package org.hswebframework.web.boost.bean;

@FunctionalInterface
public interface Converter {
    <T> T convert(Object source, Class<T> targetClass);
}