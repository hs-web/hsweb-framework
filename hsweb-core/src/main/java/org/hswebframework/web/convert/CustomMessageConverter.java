package org.hswebframework.web.convert;

/**
 * @author zhouhao
 * @since
 */
public interface CustomMessageConverter {
    boolean support(Class clazz);

    Object convert(Class clazz, byte[] message);
}
