package org.hswebframework.web.convert;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface CustomMessageConverter {
    boolean support(Class clazz);

    Object convert(Class clazz, byte[] message);
}
